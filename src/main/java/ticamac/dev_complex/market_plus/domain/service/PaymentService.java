package ticamac.dev_complex.market_plus.domain.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import ticamac.dev_complex.market_plus.application.dto.payment.InitiatePaymentRequest;
import ticamac.dev_complex.market_plus.application.dto.payment.PaymentResponse;
import ticamac.dev_complex.market_plus.domain.model.*;
import ticamac.dev_complex.market_plus.domain.port.in.PaymentUseCase;
import ticamac.dev_complex.market_plus.domain.port.out.OrderRepositoryPort;
import ticamac.dev_complex.market_plus.domain.port.out.PaymentRepositoryPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService implements PaymentUseCase {

    private final PaymentRepositoryPort paymentRepository;
    private final OrderRepositoryPort orderRepository;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${momo.api-url:https://sandbox.momodeveloper.mtn.com}")
    private String momoApiUrl;

    @Value("${momo.subscription-key:}")
    private String momoSubscriptionKey;

    public PaymentService(PaymentRepositoryPort paymentRepository,
            OrderRepositoryPort orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // ─── STRIPE ──────────────────────────────────────────────

    @Override
    public PaymentResponse initiateStripePayment(UUID orderId, UUID userId) {
        Order order = getOrderForUser(orderId, userId);

        Stripe.apiKey = stripeSecretKey;

        try {
            // Convertir en centimes (Stripe utilise la plus petite unité monétaire)
            long amountInCents = order.getTotal()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("xaf") // Franc CFA — adapte selon ta devise
                    .putMetadata("orderId", orderId.toString())
                    .putMetadata("userId", userId.toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Sauvegarder le paiement en base
            Payment payment = createPayment(order, PaymentProvider.STRIPE,
                    intent.getId(), PaymentStatus.PENDING);
            Payment saved = paymentRepository.save(payment);

            // Construire la réponse
            PaymentResponse response = buildResponse(saved, order);
            response.setClientSecret(intent.getClientSecret());
            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Erreur Stripe : " + e.getMessage(), e);
        }
    }

    @Override
    public Payment handleStripeWebhook(String payload, String signature) {
        Stripe.apiKey = stripeSecretKey;

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Signature Stripe invalide.");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) event
                    .getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Impossible de désérialiser l'événement."));

            Payment payment = paymentRepository.findByTransactionId(intent.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable."));

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(OffsetDateTime.now());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("intentId", intent.getId());
            responseData.put("status", intent.getStatus());
            payment.setProviderResponse(responseData);

            Payment updated = paymentRepository.save(payment);

            // Mettre à jour le statut de la commande
            orderRepository.findById(payment.getOrderId()).ifPresent(order -> {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
            });

            return updated;
        }

        if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) event
                    .getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow();

            paymentRepository.findByTransactionId(intent.getId()).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            });
        }

        return null;
    }

    // ─── MTN MOMO ────────────────────────────────────────────

    @Override
    public PaymentResponse initiateMomoPayment(UUID orderId, UUID userId,
            InitiatePaymentRequest request) {
        Order order = getOrderForUser(orderId, userId);

        // Génère un UUID de référence unique pour cette transaction MoMo
        String referenceId = UUID.randomUUID().toString();

        // Appel à l'API MoMo (Request to Pay)
        // En production, tu appelleras le vrai endpoint MoMo ici
        // Pour le moment on crée le paiement en PENDING
        Payment payment = createPayment(order, PaymentProvider.MTN_MOMO,
                referenceId, PaymentStatus.PENDING);

        Map<String, Object> meta = new HashMap<>();
        meta.put("referenceId", referenceId);
        meta.put("phoneNumber", request.getPhoneNumber());
        payment.setProviderResponse(meta);

        Payment saved = paymentRepository.save(payment);

        PaymentResponse response = buildResponse(saved, order);
        response.setReferenceId(referenceId);
        response.setPaymentUrl(momoApiUrl + "/collection/v1_0/requesttopay/" + referenceId);
        return response;
    }

    @Override
    public Payment handleMomoCallback(String referenceId, String status) {
        Payment payment = paymentRepository.findByTransactionId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement MoMo introuvable."));

        if ("SUCCESSFUL".equals(status)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(OffsetDateTime.now());
            orderRepository.findById(payment.getOrderId()).ifPresent(order -> {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
            });
        } else if ("FAILED".equals(status) || "REJECTED".equals(status)) {
            payment.setStatus(PaymentStatus.FAILED);
        }

        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentByOrder(UUID orderId, UUID userId) {
        getOrderForUser(orderId, userId);
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable."));
    }

    // ─── Helpers ─────────────────────────────────────────────

    private Order getOrderForUser(UUID orderId, UUID userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable."));
    }

    private Payment createPayment(Order order, PaymentProvider provider,
            String transactionId, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setProvider(provider);
        payment.setTransactionId(transactionId);
        payment.setAmount(order.getTotal());
        payment.setStatus(status);
        return payment;
    }

    private PaymentResponse buildResponse(Payment payment, Order order) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getId());
        response.setOrderId(order.getId());
        response.setProvider(payment.getProvider());
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        return response;
    }
}
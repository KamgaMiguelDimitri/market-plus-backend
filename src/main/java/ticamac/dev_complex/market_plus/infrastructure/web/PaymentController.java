package ticamac.dev_complex.market_plus.infrastructure.web;

import ticamac.dev_complex.market_plus.application.dto.payment.InitiatePaymentRequest;
import ticamac.dev_complex.market_plus.application.dto.payment.PaymentResponse;
import ticamac.dev_complex.market_plus.domain.model.Payment;
import ticamac.dev_complex.market_plus.domain.port.in.PaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    public PaymentController(PaymentUseCase paymentUseCase) {
        this.paymentUseCase = paymentUseCase;
    }

    // ── Initier un paiement Stripe ───────────────────────────
    @PostMapping("/stripe/{orderId}")
    public ResponseEntity<PaymentResponse> stripePayment(@PathVariable UUID orderId,
            Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(paymentUseCase.initiateStripePayment(orderId, userId));
    }

    // ── Initier un paiement MoMo ─────────────────────────────
    @PostMapping("/momo/{orderId}")
    public ResponseEntity<PaymentResponse> momoPayment(@PathVariable UUID orderId,
            @Valid @RequestBody InitiatePaymentRequest request,
            Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(
                paymentUseCase.initiateMomoPayment(orderId, userId, request));
    }

    // ── Webhook Stripe (pas d'auth — appelé par Stripe) ─────
    @PostMapping("/webhooks/stripe")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        paymentUseCase.handleStripeWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    // ── Callback MoMo (pas d'auth — appelé par MTN) ─────────
    @PutMapping("/webhooks/momo/{referenceId}")
    public ResponseEntity<Void> momoCallback(
            @PathVariable String referenceId,
            @RequestParam String status) {
        paymentUseCase.handleMomoCallback(referenceId, status);
        return ResponseEntity.ok().build();
    }

    // ── Statut d'un paiement ─────────────────────────────────
    @GetMapping("/{orderId}")
    public ResponseEntity<Payment> getPayment(@PathVariable UUID orderId,
            Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(paymentUseCase.getPaymentByOrder(orderId, userId));
    }
}
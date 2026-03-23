package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.domain.model.*;
import ticamac.dev_complex.market_plus.domain.port.out.OrderRepositoryPort;
import ticamac.dev_complex.market_plus.domain.port.out.PaymentRepositoryPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService — tests unitaires")
class PaymentServiceTest {

    @Mock
    private PaymentRepositoryPort paymentRepository;
    @Mock
    private OrderRepositoryPort orderRepository;
    @InjectMocks
    private PaymentService paymentService;

    private UUID userId;
    private UUID orderId;
    private Order order;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(new BigDecimal("99.99"));

        // Injecter les @Value via ReflectionTestUtils
        ReflectionTestUtils.setField(paymentService, "stripeSecretKey", "sk_test_fake");
        ReflectionTestUtils.setField(paymentService, "stripeWebhookSecret", "whsec_fake");
        ReflectionTestUtils.setField(paymentService, "momoApiUrl",
                "https://sandbox.momodeveloper.mtn.com");
        ReflectionTestUtils.setField(paymentService, "momoSubscriptionKey", "fake_key");
    }

    // ─── MoMo ──────────────────────────────────────────────

    @Nested
    @DisplayName("initiateMomoPayment()")
    class MomoPaymentTests {

        @Test
        @DisplayName("doit créer un paiement MoMo en PENDING")
        void initiateMomoPayment_createsPendingPayment() {
            var request = new ticamac.dev_complex.market_plus.application.dto.payment.InitiatePaymentRequest();
            request.setPhoneNumber("237670000000");

            Payment savedPayment = new Payment();
            savedPayment.setId(UUID.randomUUID());
            savedPayment.setOrderId(orderId);
            savedPayment.setProvider(PaymentProvider.MTN_MOMO);
            savedPayment.setStatus(PaymentStatus.PENDING);
            savedPayment.setAmount(order.getTotal());

            when(orderRepository.findByIdAndUserId(orderId, userId))
                    .thenReturn(Optional.of(order));
            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

            var response = paymentService.initiateMomoPayment(orderId, userId, request);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(response.getProvider()).isEqualTo(PaymentProvider.MTN_MOMO);
            assertThat(response.getReferenceId()).isNotNull();
        }

        @Test
        @DisplayName("doit rejeter si la commande n'appartient pas à l'utilisateur")
        void initiateMomoPayment_wrongUser_throwsException() {
            var request = new ticamac.dev_complex.market_plus.application.dto.payment.InitiatePaymentRequest();
            request.setPhoneNumber("237670000000");

            when(orderRepository.findByIdAndUserId(orderId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.initiateMomoPayment(orderId, userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Commande introuvable");
        }
    }

    // ─── MoMo Callback ─────────────────────────────────────

    @Nested
    @DisplayName("handleMomoCallback()")
    class MomoCallbackTests {

        @Test
        @DisplayName("doit passer le paiement en SUCCESS et la commande en PAID")
        void handleMomoCallback_successful_updatesPaymentAndOrder() {
            String referenceId = UUID.randomUUID().toString();

            Payment payment = new Payment();
            payment.setId(UUID.randomUUID());
            payment.setOrderId(orderId);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(referenceId);

            when(paymentRepository.findByTransactionId(referenceId))
                    .thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Payment result = paymentService.handleMomoCallback(referenceId, "SUCCESSFUL");

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(result.getPaidAt()).isNotNull();
            verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.PAID));
        }

        @Test
        @DisplayName("doit passer le paiement en FAILED si rejeté")
        void handleMomoCallback_rejected_setsFailedStatus() {
            String referenceId = UUID.randomUUID().toString();

            Payment payment = new Payment();
            payment.setId(UUID.randomUUID());
            payment.setOrderId(orderId);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(referenceId);

            when(paymentRepository.findByTransactionId(referenceId))
                    .thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Payment result = paymentService.handleMomoCallback(referenceId, "REJECTED");

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("doit rejeter un referenceId introuvable")
        void handleMomoCallback_unknownReference_throwsException() {
            when(paymentRepository.findByTransactionId(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.handleMomoCallback("unknown-ref", "SUCCESSFUL"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("MoMo introuvable");
        }
    }

    // ─── getPaymentByOrder ─────────────────────────────────

    @Nested
    @DisplayName("getPaymentByOrder()")
    class GetPaymentTests {

        @Test
        @DisplayName("doit retourner le paiement d'une commande")
        void getPaymentByOrder_success() {
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID());
            payment.setOrderId(orderId);
            payment.setStatus(PaymentStatus.SUCCESS);

            when(orderRepository.findByIdAndUserId(orderId, userId))
                    .thenReturn(Optional.of(order));
            when(paymentRepository.findByOrderId(orderId))
                    .thenReturn(Optional.of(payment));

            Payment result = paymentService.getPaymentByOrder(orderId, userId);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }
    }
}
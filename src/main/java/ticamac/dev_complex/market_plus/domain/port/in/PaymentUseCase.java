package ticamac.dev_complex.market_plus.domain.port.in;

import ticamac.dev_complex.market_plus.application.dto.payment.InitiatePaymentRequest;
import ticamac.dev_complex.market_plus.application.dto.payment.PaymentResponse;
import ticamac.dev_complex.market_plus.domain.model.Payment;

import java.util.UUID;

public interface PaymentUseCase {

    PaymentResponse initiateStripePayment(UUID orderId, UUID userId);

    PaymentResponse initiateMomoPayment(UUID orderId, UUID userId,
            InitiatePaymentRequest request);

    Payment handleStripeWebhook(String payload, String signature);

    Payment handleMomoCallback(String referenceId, String status);

    Payment getPaymentByOrder(UUID orderId, UUID userId);
}
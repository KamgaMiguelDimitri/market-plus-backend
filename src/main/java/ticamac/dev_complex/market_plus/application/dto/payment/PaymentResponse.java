package ticamac.dev_complex.market_plus.application.dto.payment;

import ticamac.dev_complex.market_plus.domain.model.PaymentProvider;
import ticamac.dev_complex.market_plus.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentResponse {

    private UUID paymentId;
    private UUID orderId;
    private PaymentProvider provider;
    private PaymentStatus status;
    private BigDecimal amount;

    // Stripe : client secret pour le frontend
    private String clientSecret;

    // MoMo : lien de paiement ou référence
    private String paymentUrl;
    private String referenceId;

    public PaymentResponse() {
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public void setProvider(PaymentProvider provider) {
        this.provider = provider;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
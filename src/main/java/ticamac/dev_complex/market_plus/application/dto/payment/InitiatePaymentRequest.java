package ticamac.dev_complex.market_plus.application.dto.payment;

import jakarta.validation.constraints.NotBlank;

public class InitiatePaymentRequest {

    @NotBlank(message = "Le numéro de téléphone est obligatoire.")
    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
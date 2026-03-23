package ticamac.dev_complex.market_plus.application.dto.order;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateOrderRequest {

    @NotNull(message = "L'adresse de livraison est obligatoire.")
    private UUID shippingAddressId;

    private UUID promotionId;

    public UUID getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(UUID shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public UUID getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(UUID promotionId) {
        this.promotionId = promotionId;
    }
}
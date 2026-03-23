package ticamac.dev_complex.market_plus.application.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AddToCartRequest {

    @NotNull(message = "La variante est obligatoire.")
    private UUID variantId;

    @Min(value = 1, message = "La quantité doit être au moins 1.")
    private int quantity = 1;

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
package ticamac.dev_complex.market_plus.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class CartItem {

    private UUID id;
    private UUID cartId;
    private UUID variantId;
    private int quantity;
    private BigDecimal unitPrice;

    // Snapshot du produit pour l'affichage
    private String productName;
    private String productSlug;
    private String imageUrl;
    private java.util.Map<String, Object> variantAttributes;

    public CartItem() {
    }

    public BigDecimal getSubtotal() {
        if (unitPrice == null)
            return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSlug() {
        return productSlug;
    }

    public void setProductSlug(String productSlug) {
        this.productSlug = productSlug;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public java.util.Map<String, Object> getVariantAttributes() {
        return variantAttributes;
    }

    public void setVariantAttributes(java.util.Map<String, Object> variantAttributes) {
        this.variantAttributes = variantAttributes;
    }
}
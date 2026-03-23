package ticamac.dev_complex.market_plus.domain.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class ProductVariant {

    private UUID id;
    private UUID productId;
    private String sku;
    private BigDecimal price;
    private int stock;
    private Map<String, Object> attributes;

    public ProductVariant() {
    }

    public boolean isInStock() {
        return stock > 0;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
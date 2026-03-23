package ticamac.dev_complex.market_plus.application.dto.catalogue;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public class ProductRequest {

    @NotBlank(message = "Le nom est obligatoire.")
    private String name;

    private String description;

    @NotNull(message = "Le prix est obligatoire.")
    @DecimalMin(value = "0.0", message = "Le prix doit être positif.")
    private BigDecimal basePrice;

    @NotNull(message = "La catégorie est obligatoire.")
    private UUID categoryId;

    private boolean active = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
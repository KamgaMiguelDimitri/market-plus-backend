package ticamac.dev_complex.market_plus.application.dto.order;

import ticamac.dev_complex.market_plus.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateOrderStatusRequest {

    @NotNull(message = "Le statut est obligatoire.")
    private OrderStatus status;

    private String comment;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
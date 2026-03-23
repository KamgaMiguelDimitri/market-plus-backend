package ticamac.dev_complex.market_plus.domain.port.in;

import ticamac.dev_complex.market_plus.application.dto.order.CreateOrderRequest;
import ticamac.dev_complex.market_plus.application.dto.order.UpdateOrderStatusRequest;
import ticamac.dev_complex.market_plus.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderUseCase {

    Order createOrder(UUID userId, CreateOrderRequest request);

    Page<Order> getUserOrders(UUID userId, Pageable pageable);

    Order getOrderById(UUID orderId, UUID userId);

    Order updateStatus(UUID orderId, UpdateOrderStatusRequest request);

    Order cancelOrder(UUID orderId, UUID userId);

    Page<Order> getAllOrders(Pageable pageable);
}
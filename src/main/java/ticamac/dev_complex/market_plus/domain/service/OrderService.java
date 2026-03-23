package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.application.dto.order.CreateOrderRequest;
import ticamac.dev_complex.market_plus.application.dto.order.UpdateOrderStatusRequest;
import ticamac.dev_complex.market_plus.domain.model.*;
import ticamac.dev_complex.market_plus.domain.port.in.OrderUseCase;
import ticamac.dev_complex.market_plus.domain.port.out.CartRepositoryPort;
import ticamac.dev_complex.market_plus.domain.port.out.OrderRepositoryPort;
import ticamac.dev_complex.market_plus.domain.port.out.ProductRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService implements OrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final CartRepositoryPort cartRepository;
    private final ProductRepositoryPort productRepository;

    public OrderService(OrderRepositoryPort orderRepository,
            CartRepositoryPort cartRepository,
            ProductRepositoryPort productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Order createOrder(UUID userId, CreateOrderRequest request) {
        // 1. Récupérer le panier
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Panier introuvable."));

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Impossible de commander avec un panier vide.");
        }

        // 2. Construire les OrderItems depuis le panier
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            ProductVariant variant = productRepository.findVariantById(cartItem.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Variante introuvable : " + cartItem.getVariantId()));

            if (variant.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Stock insuffisant pour la variante : " + variant.getSku());
            }

            // Récupérer le nom du produit via le productId de la variante
            Product product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Produit introuvable."));

            OrderItem item = new OrderItem();
            item.setVariantId(cartItem.getVariantId());
            item.setProductName(product.getName());
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(cartItem.getUnitPrice());
            item.setTotal(cartItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            return item;
        }).collect(Collectors.toList());

        // 3. Calculer les montants
        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingCost = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal total = subtotal.add(shippingCost).add(taxAmount);

        // 4. Créer la commande
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddressId(request.getShippingAddressId());
        order.setPromotionId(request.getPromotionId());
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setShippingCost(shippingCost);
        order.setTaxAmount(taxAmount);
        order.setTotal(total);
        order.setCreatedAt(OffsetDateTime.now());
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);

        // 5. Vider le panier après commande
        cartRepository.deleteAllItems(cart.getId());

        return saved;
    }

    @Override
    public Page<Order> getUserOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    public Order getOrderById(UUID orderId, UUID userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable."));
    }

    @Override
    public Order updateStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable."));

        order.setStatus(request.getStatus());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId, UUID userId) {
        Order order = getOrderById(orderId, userId);

        if (!order.isCancellable()) {
            throw new IllegalArgumentException(
                    "Cette commande ne peut plus être annulée (statut : " + order.getStatus() + ").");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}
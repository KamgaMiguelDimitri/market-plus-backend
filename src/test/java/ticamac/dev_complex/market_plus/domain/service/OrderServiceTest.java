package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.application.dto.order.CreateOrderRequest;
import ticamac.dev_complex.market_plus.application.dto.order.UpdateOrderStatusRequest;
import ticamac.dev_complex.market_plus.domain.model.*;
import ticamac.dev_complex.market_plus.domain.port.out.CartRepositoryPort;
import ticamac.dev_complex.market_plus.domain.port.out.OrderRepositoryPort;
import ticamac.dev_complex.market_plus.domain.port.out.ProductRepositoryPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService — tests unitaires")
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private CartRepositoryPort cartRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @InjectMocks
    private OrderService orderService;

    private UUID userId;
    private UUID orderId;
    private Cart cartWithItems;
    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        variant = new ProductVariant();
        variant.setId(UUID.randomUUID());
        variant.setProductId(UUID.randomUUID());
        variant.setSku("SKU-001");
        variant.setPrice(new BigDecimal("49.99"));
        variant.setStock(10);

        product = new Product();
        product.setId(variant.getProductId());
        product.setName("Produit Test");
        product.setSlug("produit-test");

        CartItem cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        cartItem.setVariantId(variant.getId());
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(variant.getPrice());

        cartWithItems = new Cart();
        cartWithItems.setId(UUID.randomUUID());
        cartWithItems.setUserId(userId);
        cartWithItems.setItems(List.of(cartItem));
        cartWithItems.setUpdatedAt(OffsetDateTime.now());
    }

    // ─── createOrder ───────────────────────────────────────

    @Nested
    @DisplayName("createOrder()")
    class CreateOrderTests {

        @Test
        @DisplayName("doit créer une commande depuis le panier")
        void createOrder_success() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddressId(UUID.randomUUID());

            Order savedOrder = new Order();
            savedOrder.setId(orderId);
            savedOrder.setStatus(OrderStatus.PENDING);
            savedOrder.setTotal(new BigDecimal("99.98"));

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartWithItems));
            when(productRepository.findVariantById(variant.getId())).thenReturn(Optional.of(variant));
            when(productRepository.findById(variant.getProductId())).thenReturn(Optional.of(product));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            Order result = orderService.createOrder(userId, request);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
            verify(cartRepository).deleteAllItems(cartWithItems.getId());
        }

        @Test
        @DisplayName("doit rejeter un panier vide")
        void createOrder_emptyCart_throwsException() {
            Cart emptyCart = new Cart();
            emptyCart.setId(UUID.randomUUID());
            emptyCart.setUserId(userId);
            emptyCart.setItems(new ArrayList<>());

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));

            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddressId(UUID.randomUUID());

            assertThatThrownBy(() -> orderService.createOrder(userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("panier vide");
        }

        @Test
        @DisplayName("doit rejeter si stock insuffisant")
        void createOrder_insufficientStock_throwsException() {
            variant.setStock(1);
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddressId(UUID.randomUUID());

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartWithItems));
            when(productRepository.findVariantById(variant.getId())).thenReturn(Optional.of(variant));

            assertThatThrownBy(() -> orderService.createOrder(userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock insuffisant");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("doit vider le panier après création de la commande")
        void createOrder_clearsCartAfterCreation() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddressId(UUID.randomUUID());

            Order savedOrder = new Order();
            savedOrder.setId(orderId);
            savedOrder.setStatus(OrderStatus.PENDING);
            savedOrder.setTotal(new BigDecimal("99.98"));

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartWithItems));
            when(productRepository.findVariantById(variant.getId())).thenReturn(Optional.of(variant));
            when(productRepository.findById(variant.getProductId())).thenReturn(Optional.of(product));
            when(orderRepository.save(any())).thenReturn(savedOrder);

            orderService.createOrder(userId, request);

            verify(cartRepository).deleteAllItems(cartWithItems.getId());
        }
    }

    // ─── cancelOrder ───────────────────────────────────────

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrderTests {

        @Test
        @DisplayName("doit annuler une commande PENDING")
        void cancelOrder_pending_success() {
            Order order = buildOrder(OrderStatus.PENDING);
            when(orderRepository.findByIdAndUserId(orderId, userId))
                    .thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.cancelOrder(orderId, userId);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("doit annuler une commande PAID")
        void cancelOrder_paid_success() {
            Order order = buildOrder(OrderStatus.PAID);
            when(orderRepository.findByIdAndUserId(orderId, userId))
                    .thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.cancelOrder(orderId, userId);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("doit rejeter l'annulation d'une commande SHIPPED")
        void cancelOrder_shipped_throwsException() {
            Order order = buildOrder(OrderStatus.SHIPPED);
            when(orderRepository.findByIdAndUserId(orderId, userId))
                    .thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ne peut plus être annulée");
        }

        @Test
        @DisplayName("doit rejeter l'annulation d'une commande DELIVERED")
        void cancelOrder_delivered_throwsException() {
            Order order = buildOrder(OrderStatus.DELIVERED);
            when(orderRepository.findByIdAndUserId(orderId, userId))
                    .thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ne peut plus être annulée");
        }
    }

    // ─── updateStatus ──────────────────────────────────────

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatusTests {

        @Test
        @DisplayName("doit changer le statut d'une commande")
        void updateStatus_success() {
            Order order = buildOrder(OrderStatus.PAID);
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus(OrderStatus.PROCESSING);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateStatus(orderId, request);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        }
    }

    private Order buildOrder(OrderStatus status) {
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(status);
        order.setTotal(new BigDecimal("99.98"));
        return order;
    }
}
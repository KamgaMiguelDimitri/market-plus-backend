package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.application.dto.cart.AddToCartRequest;
import ticamac.dev_complex.market_plus.application.dto.cart.UpdateCartItemRequest;
import ticamac.dev_complex.market_plus.domain.model.Cart;
import ticamac.dev_complex.market_plus.domain.model.CartItem;
import ticamac.dev_complex.market_plus.domain.model.ProductVariant;
import ticamac.dev_complex.market_plus.domain.port.out.CartRepositoryPort;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService — tests unitaires")
class CartServiceTest {

    @Mock
    private CartRepositoryPort cartRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @InjectMocks
    private CartService cartService;

    private UUID userId;
    private UUID variantId;
    private ProductVariant variant;
    private Cart existingCart;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        variantId = UUID.randomUUID();

        variant = new ProductVariant();
        variant.setId(variantId);
        variant.setSku("SKU-001");
        variant.setPrice(new BigDecimal("29.99"));
        variant.setStock(10);

        existingCart = new Cart();
        existingCart.setId(UUID.randomUUID());
        existingCart.setUserId(userId);
        existingCart.setItems(new ArrayList<>());
        existingCart.setUpdatedAt(OffsetDateTime.now());
    }

    // ─── getCart ───────────────────────────────────────────

    @Nested
    @DisplayName("getCart()")
    class GetCartTests {

        @Test
        @DisplayName("doit retourner le panier existant")
        void getCart_returnsExistingCart() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

            Cart result = cartService.getCart(userId);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("doit créer un panier vide si inexistant")
        void getCart_createsEmptyCartIfNotExists() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

            Cart result = cartService.getCart(userId);

            assertThat(result).isNotNull();
            verify(cartRepository).save(any(Cart.class));
        }
    }

    // ─── addItem ───────────────────────────────────────────

    @Nested
    @DisplayName("addItem()")
    class AddItemTests {

        @Test
        @DisplayName("doit ajouter un article au panier")
        void addItem_success() {
            AddToCartRequest request = new AddToCartRequest();
            request.setVariantId(variantId);
            request.setQuantity(2);

            when(productRepository.findVariantById(variantId)).thenReturn(Optional.of(variant));
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
            when(cartRepository.findItemByCartAndVariant(any(), any())).thenReturn(Optional.empty());
            when(cartRepository.saveItem(any())).thenAnswer(inv -> inv.getArgument(0));
            when(cartRepository.save(any())).thenReturn(existingCart);

            cartService.addItem(userId, request);

            verify(cartRepository).saveItem(any(CartItem.class));
        }

        @Test
        @DisplayName("doit rejeter si stock insuffisant")
        void addItem_insufficientStock_throwsException() {
            variant.setStock(1);
            AddToCartRequest request = new AddToCartRequest();
            request.setVariantId(variantId);
            request.setQuantity(5);

            when(productRepository.findVariantById(variantId)).thenReturn(Optional.of(variant));

            assertThatThrownBy(() -> cartService.addItem(userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock insuffisant");
        }

        @Test
        @DisplayName("doit incrémenter si variante déjà dans le panier")
        void addItem_existingVariant_incrementsQuantity() {
            AddToCartRequest request = new AddToCartRequest();
            request.setVariantId(variantId);
            request.setQuantity(1);

            CartItem existingItem = new CartItem();
            existingItem.setId(UUID.randomUUID());
            existingItem.setVariantId(variantId);
            existingItem.setQuantity(2);
            existingItem.setUnitPrice(variant.getPrice());

            when(productRepository.findVariantById(variantId)).thenReturn(Optional.of(variant));
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
            when(cartRepository.findItemByCartAndVariant(any(), any()))
                    .thenReturn(Optional.of(existingItem));
            when(cartRepository.saveItem(any())).thenAnswer(inv -> inv.getArgument(0));
            when(cartRepository.save(any())).thenReturn(existingCart);

            cartService.addItem(userId, request);

            verify(cartRepository).saveItem(argThat(item -> item.getQuantity() == 3));
        }

        @Test
        @DisplayName("doit rejeter une variante introuvable")
        void addItem_variantNotFound_throwsException() {
            AddToCartRequest request = new AddToCartRequest();
            request.setVariantId(variantId);
            request.setQuantity(1);

            when(productRepository.findVariantById(variantId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItem(userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Variante introuvable");
        }
    }

    // ─── updateItem ────────────────────────────────────────

    @Nested
    @DisplayName("updateItem()")
    class UpdateItemTests {

        @Test
        @DisplayName("doit mettre à jour la quantité")
        void updateItem_success() {
            UUID itemId = UUID.randomUUID();
            CartItem item = new CartItem();
            item.setId(itemId);
            item.setVariantId(variantId);
            item.setQuantity(1);

            UpdateCartItemRequest request = new UpdateCartItemRequest();
            request.setQuantity(3);

            when(cartRepository.findItemById(itemId)).thenReturn(Optional.of(item));
            when(productRepository.findVariantById(variantId)).thenReturn(Optional.of(variant));
            when(cartRepository.saveItem(any())).thenAnswer(inv -> inv.getArgument(0));
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

            cartService.updateItem(userId, itemId, request);

            verify(cartRepository).saveItem(argThat(i -> i.getQuantity() == 3));
        }

        @Test
        @DisplayName("doit rejeter si stock insuffisant lors de la mise à jour")
        void updateItem_insufficientStock_throwsException() {
            variant.setStock(2);
            UUID itemId = UUID.randomUUID();
            CartItem item = new CartItem();
            item.setId(itemId);
            item.setVariantId(variantId);
            item.setQuantity(1);

            UpdateCartItemRequest request = new UpdateCartItemRequest();
            request.setQuantity(5);

            when(cartRepository.findItemById(itemId)).thenReturn(Optional.of(item));
            when(productRepository.findVariantById(variantId)).thenReturn(Optional.of(variant));

            assertThatThrownBy(() -> cartService.updateItem(userId, itemId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock insuffisant");
        }
    }

    // ─── removeItem & clearCart ────────────────────────────

    @Nested
    @DisplayName("removeItem() / clearCart()")
    class RemoveTests {

        @Test
        @DisplayName("doit supprimer un article")
        void removeItem_success() {
            UUID itemId = UUID.randomUUID();
            CartItem item = new CartItem();
            item.setId(itemId);

            when(cartRepository.findItemById(itemId)).thenReturn(Optional.of(item));
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

            cartService.removeItem(userId, itemId);

            verify(cartRepository).deleteItem(itemId);
        }

        @Test
        @DisplayName("doit vider le panier")
        void clearCart_success() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

            cartService.clearCart(userId);

            verify(cartRepository).deleteAllItems(existingCart.getId());
        }

        @Test
        @DisplayName("ne doit rien faire si le panier est inexistant")
        void clearCart_noCart_doesNothing() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

            cartService.clearCart(userId);

            verify(cartRepository, never()).deleteAllItems(any());
        }
    }
}
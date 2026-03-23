package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.application.dto.cart.AddToCartRequest;
import ticamac.dev_complex.market_plus.application.dto.cart.UpdateCartItemRequest;
import ticamac.dev_complex.market_plus.domain.model.Cart;
import ticamac.dev_complex.market_plus.domain.model.CartItem;
import ticamac.dev_complex.market_plus.domain.model.ProductVariant;
import ticamac.dev_complex.market_plus.domain.port.in.CartUseCase;
import ticamac.dev_complex.market_plus.domain.port.out.CartRepositoryPort;
import ticamac.dev_complex.market_plus.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService implements CartUseCase {

    private final CartRepositoryPort cartRepository;
    private final ProductRepositoryPort productRepository;

    public CartService(CartRepositoryPort cartRepository,
            ProductRepositoryPort productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Cart getCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
    }

    @Override
    public Cart addItem(UUID userId, AddToCartRequest request) {
        // Vérifier que la variante existe et a du stock
        ProductVariant variant = productRepository.findVariantById(request.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable."));

        if (variant.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Stock insuffisant. Disponible : " + variant.getStock());
        }

        // Récupérer ou créer le panier
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));

        // Si la variante est déjà dans le panier, incrémenter la quantité
        Optional<CartItem> existingItem = cartRepository
                .findItemByCartAndVariant(cart.getId(), request.getVariantId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (variant.getStock() < newQty) {
                throw new IllegalArgumentException(
                        "Stock insuffisant. Disponible : " + variant.getStock());
            }
            item.setQuantity(newQty);
            cartRepository.saveItem(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCartId(cart.getId());
            newItem.setVariantId(request.getVariantId());
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(variant.getPrice());
            cartRepository.saveItem(newItem);
        }

        cart.setUpdatedAt(OffsetDateTime.now());
        cartRepository.save(cart);

        return getCart(userId);
    }

    @Override
    public Cart updateItem(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        CartItem item = cartRepository.findItemById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Article introuvable."));

        // Vérifier le stock
        ProductVariant variant = productRepository.findVariantById(item.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable."));

        if (variant.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Stock insuffisant. Disponible : " + variant.getStock());
        }

        item.setQuantity(request.getQuantity());
        cartRepository.saveItem(item);

        return getCart(userId);
    }

    @Override
    public Cart removeItem(UUID userId, UUID itemId) {
        cartRepository.findItemById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Article introuvable."));

        cartRepository.deleteItem(itemId);
        return getCart(userId);
    }

    @Override
    public void clearCart(UUID userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> cartRepository.deleteAllItems(cart.getId()));
    }

    private Cart createEmptyCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setUpdatedAt(OffsetDateTime.now());
        return cartRepository.save(cart);
    }
}
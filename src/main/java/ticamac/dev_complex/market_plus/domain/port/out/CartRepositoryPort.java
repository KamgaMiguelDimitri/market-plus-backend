package ticamac.dev_complex.market_plus.domain.port.out;

import ticamac.dev_complex.market_plus.domain.model.Cart;
import ticamac.dev_complex.market_plus.domain.model.CartItem;

import java.util.Optional;
import java.util.UUID;

public interface CartRepositoryPort {

    Optional<Cart> findByUserId(UUID userId);

    Cart save(Cart cart);

    CartItem saveItem(CartItem item);

    void deleteItem(UUID itemId);

    void deleteAllItems(UUID cartId);

    Optional<CartItem> findItemById(UUID itemId);

    Optional<CartItem> findItemByCartAndVariant(UUID cartId, UUID variantId);
}
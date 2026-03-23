package ticamac.dev_complex.market_plus.domain.port.in;

import ticamac.dev_complex.market_plus.application.dto.cart.AddToCartRequest;
import ticamac.dev_complex.market_plus.application.dto.cart.UpdateCartItemRequest;
import ticamac.dev_complex.market_plus.domain.model.Cart;

import java.util.UUID;

public interface CartUseCase {

    Cart getCart(UUID userId);

    Cart addItem(UUID userId, AddToCartRequest request);

    Cart updateItem(UUID userId, UUID itemId, UpdateCartItemRequest request);

    Cart removeItem(UUID userId, UUID itemId);

    void clearCart(UUID userId);
}
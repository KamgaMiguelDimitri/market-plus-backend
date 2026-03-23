package ticamac.dev_complex.market_plus.infrastructure.persistence.adapter;

import ticamac.dev_complex.market_plus.domain.model.Cart;
import ticamac.dev_complex.market_plus.domain.model.CartItem;
import ticamac.dev_complex.market_plus.domain.port.out.CartRepositoryPort;
import ticamac.dev_complex.market_plus.infrastructure.persistence.mapper.CartMapper;
import ticamac.dev_complex.market_plus.infrastructure.persistence.repository.CartItemJpaRepository;
import ticamac.dev_complex.market_plus.infrastructure.persistence.repository.CartJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class CartRepositoryAdapter implements CartRepositoryPort {

    private final CartJpaRepository cartJpa;
    private final CartItemJpaRepository itemJpa;
    private final CartMapper mapper;

    public CartRepositoryAdapter(CartJpaRepository cartJpa,
            CartItemJpaRepository itemJpa,
            CartMapper mapper) {
        this.cartJpa = cartJpa;
        this.itemJpa = itemJpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        return cartJpa.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        return mapper.toDomain(cartJpa.save(mapper.toEntity(cart)));
    }

    @Override
    public CartItem saveItem(CartItem item) {
        return mapper.toDomain(itemJpa.save(mapper.toEntity(item)));
    }

    @Override
    @Transactional
    public void deleteItem(UUID itemId) {
        itemJpa.deleteById(itemId);
    }

    @Override
    @Transactional
    public void deleteAllItems(UUID cartId) {
        itemJpa.deleteAllByCartId(cartId);
    }

    @Override
    public Optional<CartItem> findItemById(UUID itemId) {
        return itemJpa.findById(itemId).map(mapper::toDomain);
    }

    @Override
    public Optional<CartItem> findItemByCartAndVariant(UUID cartId, UUID variantId) {
        return itemJpa.findByCartIdAndVariantId(cartId, variantId).map(mapper::toDomain);
    }
}
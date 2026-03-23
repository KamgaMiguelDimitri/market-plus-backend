package ticamac.dev_complex.market_plus.infrastructure.persistence.repository;

import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, UUID> {

    Optional<CartItemEntity> findByCartIdAndVariantId(UUID cartId, UUID variantId);

    void deleteAllByCartId(UUID cartId);
}
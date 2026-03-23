package ticamac.dev_complex.market_plus.infrastructure.persistence.mapper;

import ticamac.dev_complex.market_plus.domain.model.Cart;
import ticamac.dev_complex.market_plus.domain.model.CartItem;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.CartEntity;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.CartItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "items", target = "items")
    Cart toDomain(CartEntity entity);

    @Mapping(source = "items", target = "items")
    CartEntity toEntity(Cart domain);

    CartItem toDomain(CartItemEntity entity);

    CartItemEntity toEntity(CartItem domain);
}
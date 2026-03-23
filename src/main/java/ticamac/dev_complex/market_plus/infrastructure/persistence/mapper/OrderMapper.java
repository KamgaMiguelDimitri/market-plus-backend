package ticamac.dev_complex.market_plus.infrastructure.persistence.mapper;

import ticamac.dev_complex.market_plus.domain.model.Order;
import ticamac.dev_complex.market_plus.domain.model.OrderItem;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.OrderEntity;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.OrderItemEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toDomain(OrderEntity entity);

    OrderEntity toEntity(Order domain);

    OrderItem toDomain(OrderItemEntity entity);

    OrderItemEntity toEntity(OrderItem domain);
}
package ticamac.dev_complex.market_plus.infrastructure.persistence.mapper;

import ticamac.dev_complex.market_plus.domain.model.Payment;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.PaymentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment toDomain(PaymentEntity entity);

    PaymentEntity toEntity(Payment domain);
}
package ticamac.dev_complex.market_plus.infrastructure.persistence.mapper;

import ticamac.dev_complex.market_plus.domain.model.User;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "active", target = "active")
    User toDomain(UserEntity entity);

    @Mapping(source = "active", target = "active")
    UserEntity toEntity(User user);
}
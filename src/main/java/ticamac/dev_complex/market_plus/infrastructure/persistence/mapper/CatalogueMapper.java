package ticamac.dev_complex.market_plus.infrastructure.persistence.mapper;

import ticamac.dev_complex.market_plus.domain.model.Category;
import ticamac.dev_complex.market_plus.domain.model.Product;
import ticamac.dev_complex.market_plus.domain.model.ProductImage;
import ticamac.dev_complex.market_plus.domain.model.ProductVariant;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.CategoryEntity;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.ProductEntity;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.ProductImageEntity;
import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.ProductVariantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogueMapper {

    @Mapping(source = "active", target = "active")
    Category toDomain(CategoryEntity entity);

    @Mapping(source = "active", target = "active")
    CategoryEntity toEntity(Category domain);

    @Mapping(source = "active", target = "active")
    Product toDomain(ProductEntity entity);

    @Mapping(source = "active", target = "active")
    ProductEntity toEntity(Product domain);

    ProductVariant toDomain(ProductVariantEntity entity);

    ProductVariantEntity toEntity(ProductVariant domain);

    ProductImage toDomain(ProductImageEntity entity);

    ProductImageEntity toEntity(ProductImage domain);
}
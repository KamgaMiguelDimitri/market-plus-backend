package ticamac.dev_complex.market_plus.infrastructure.persistence.adapter;

import ticamac.dev_complex.market_plus.application.dto.catalogue.ProductFilter;
import ticamac.dev_complex.market_plus.domain.model.Product;
import ticamac.dev_complex.market_plus.domain.model.ProductVariant;
import ticamac.dev_complex.market_plus.domain.port.out.ProductRepositoryPort;
import ticamac.dev_complex.market_plus.infrastructure.persistence.mapper.CatalogueMapper;
import ticamac.dev_complex.market_plus.infrastructure.persistence.repository.ProductJpaRepository;
import ticamac.dev_complex.market_plus.infrastructure.persistence.repository.ProductSpecification;
import ticamac.dev_complex.market_plus.infrastructure.persistence.repository.ProductVariantJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpa;
    private final ProductVariantJpaRepository variantJpa;
    private final CatalogueMapper mapper;

    public ProductRepositoryAdapter(ProductJpaRepository productJpa,
            ProductVariantJpaRepository variantJpa,
            CatalogueMapper mapper) {
        this.productJpa = productJpa;
        this.variantJpa = variantJpa;
        this.mapper = mapper;
    }

    @Override
    public Page<Product> findAll(ProductFilter filter, Pageable pageable) {
        return productJpa
                .findAll(ProductSpecification.withFilter(filter), pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return productJpa.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productJpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return productJpa.existsBySlug(slug);
    }

    @Override
    public Product save(Product product) {
        return mapper.toDomain(productJpa.save(mapper.toEntity(product)));
    }

    @Override
    public void deleteById(UUID id) {
        productJpa.deleteById(id);
    }

    @Override
    public ProductVariant saveVariant(ProductVariant variant) {
        return mapper.toDomain(variantJpa.save(mapper.toEntity(variant)));
    }

    @Override
    public Optional<ProductVariant> findVariantById(UUID variantId) {
        return variantJpa.findById(variantId).map(mapper::toDomain);
    }
}
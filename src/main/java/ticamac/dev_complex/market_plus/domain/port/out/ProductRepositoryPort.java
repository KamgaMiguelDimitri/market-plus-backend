package ticamac.dev_complex.market_plus.domain.port.out;

import ticamac.dev_complex.market_plus.application.dto.catalogue.ProductFilter;
import ticamac.dev_complex.market_plus.domain.model.Product;
import ticamac.dev_complex.market_plus.domain.model.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {

    Page<Product> findAll(ProductFilter filter, Pageable pageable);

    Optional<Product> findBySlug(String slug);

    Optional<Product> findById(UUID id);

    boolean existsBySlug(String slug);

    Product save(Product product);

    void deleteById(UUID id);

    ProductVariant saveVariant(ProductVariant variant);

    Optional<ProductVariant> findVariantById(UUID variantId);
}
package ticamac.dev_complex.market_plus.domain.port.in;

import ticamac.dev_complex.market_plus.application.dto.catalogue.ProductFilter;
import ticamac.dev_complex.market_plus.application.dto.catalogue.ProductRequest;
import ticamac.dev_complex.market_plus.application.dto.catalogue.VariantRequest;
import ticamac.dev_complex.market_plus.domain.model.Product;
import ticamac.dev_complex.market_plus.domain.model.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductUseCase {

    Page<Product> getAll(ProductFilter filter, Pageable pageable);

    Product getBySlug(String slug);

    Product getById(UUID id);

    Product create(ProductRequest request, UUID adminId);

    Product update(UUID id, ProductRequest request);

    void delete(UUID id);

    ProductVariant addVariant(UUID productId, VariantRequest request);

    ProductVariant updateStock(UUID variantId, int newStock);
}
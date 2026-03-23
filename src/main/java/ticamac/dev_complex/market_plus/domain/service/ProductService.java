package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.application.dto.catalogue.ProductFilter;
import ticamac.dev_complex.market_plus.application.dto.catalogue.ProductRequest;
import ticamac.dev_complex.market_plus.application.dto.catalogue.VariantRequest;
import ticamac.dev_complex.market_plus.domain.model.Product;
import ticamac.dev_complex.market_plus.domain.model.ProductVariant;
import ticamac.dev_complex.market_plus.domain.port.in.ProductUseCase;
import ticamac.dev_complex.market_plus.domain.port.out.ProductRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort repository;

    public ProductService(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Page<Product> getAll(ProductFilter filter, Pageable pageable) {
        return repository.findAll(filter, pageable);
    }

    @Override
    public Product getBySlug(String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable : " + slug));
    }

    @Override
    public Product getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable."));
    }

    @Override
    public Product create(ProductRequest request, UUID adminId) {
        String slug = generateSlug(request.getName());
        if (repository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Un produit avec ce nom existe déjà.");
        }
        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setCategoryId(request.getCategoryId());
        product.setCreatedBy(adminId);
        product.setActive(request.isActive());
        product.setCreatedAt(OffsetDateTime.now());
        return repository.save(product);
    }

    @Override
    public Product update(UUID id, ProductRequest request) {
        Product existing = getById(id);
        existing.setName(request.getName());
        existing.setSlug(generateSlug(request.getName()));
        existing.setDescription(request.getDescription());
        existing.setBasePrice(request.getBasePrice());
        existing.setCategoryId(request.getCategoryId());
        existing.setActive(request.isActive());
        return repository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        getById(id);
        repository.deleteById(id);
    }

    @Override
    public ProductVariant addVariant(UUID productId, VariantRequest request) {
        getById(productId);
        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());
        variant.setAttributes(request.getAttributes());
        return repository.saveVariant(variant);
    }

    @Override
    public ProductVariant updateStock(UUID variantId, int newStock) {
        if (newStock < 0)
            throw new IllegalArgumentException("Le stock ne peut pas être négatif.");
        ProductVariant variant = repository.findVariantById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable."));
        variant.setStock(newStock);
        return repository.saveVariant(variant);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
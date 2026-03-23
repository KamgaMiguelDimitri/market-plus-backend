package ticamac.dev_complex.market_plus.infrastructure.web;

import ticamac.dev_complex.market_plus.application.dto.catalogue.ProductFilter;
import ticamac.dev_complex.market_plus.application.dto.catalogue.ProductRequest;
import ticamac.dev_complex.market_plus.application.dto.catalogue.VariantRequest;
import ticamac.dev_complex.market_plus.domain.model.Product;
import ticamac.dev_complex.market_plus.domain.model.ProductVariant;
import ticamac.dev_complex.market_plus.domain.port.in.ProductUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductUseCase productUseCase;

    public ProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    // ── PUBLIC ──────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<Product>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        ProductFilter filter = new ProductFilter();
        filter.setSearch(search);
        filter.setCategoryId(categoryId);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return ResponseEntity.ok(
                productUseCase.getAll(filter, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Product> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productUseCase.getBySlug(slug));
    }

    // ── ADMIN ────────────────────────────────────────

    @PostMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request,
            Authentication auth) {
        UUID adminId = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productUseCase.create(request, adminId));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Product> update(@PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productUseCase.update(id, request));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/{id}/variants")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ProductVariant> addVariant(@PathVariable UUID id,
            @Valid @RequestBody VariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productUseCase.addVariant(id, request));
    }

    @PatchMapping("/admin/variants/{variantId}/stock")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ProductVariant> updateStock(@PathVariable UUID variantId,
            @RequestParam int stock) {
        return ResponseEntity.ok(productUseCase.updateStock(variantId, stock));
    }
}
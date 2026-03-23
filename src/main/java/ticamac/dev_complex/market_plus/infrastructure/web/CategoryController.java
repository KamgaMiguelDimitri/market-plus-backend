package ticamac.dev_complex.market_plus.infrastructure.web;

import ticamac.dev_complex.market_plus.application.dto.catalogue.CategoryRequest;
import ticamac.dev_complex.market_plus.domain.model.Category;
import ticamac.dev_complex.market_plus.domain.port.in.CategoryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    public CategoryController(CategoryUseCase categoryUseCase) {
        this.categoryUseCase = categoryUseCase;
    }

    // ── PUBLIC ──────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Category>> getTree() {
        return ResponseEntity.ok(categoryUseCase.getTree());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Category> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryUseCase.getBySlug(slug));
    }

    // ── ADMIN ────────────────────────────────────────

    @PostMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryUseCase.create(request));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Category> update(@PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryUseCase.update(id, request));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
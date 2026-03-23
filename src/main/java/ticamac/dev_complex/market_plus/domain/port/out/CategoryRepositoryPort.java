package ticamac.dev_complex.market_plus.domain.port.out;

import ticamac.dev_complex.market_plus.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepositoryPort {

    List<Category> findAllActive();

    Optional<Category> findBySlug(String slug);

    Optional<Category> findById(UUID id);

    boolean existsBySlug(String slug);

    Category save(Category category);

    void deleteById(UUID id);
}
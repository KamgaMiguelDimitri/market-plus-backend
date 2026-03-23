package ticamac.dev_complex.market_plus.infrastructure.persistence.adapter;

import ticamac.dev_complex.market_plus.domain.model.Category;
import ticamac.dev_complex.market_plus.domain.port.out.CategoryRepositoryPort;
import ticamac.dev_complex.market_plus.infrastructure.persistence.mapper.CatalogueMapper;
import ticamac.dev_complex.market_plus.infrastructure.persistence.repository.CategoryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository jpa;
    private final CatalogueMapper mapper;

    public CategoryRepositoryAdapter(CategoryJpaRepository jpa, CatalogueMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<Category> findAllActive() {
        return jpa.findAllByIsActiveTrue()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return jpa.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpa.existsBySlug(slug);
    }

    @Override
    public Category save(Category category) {
        return mapper.toDomain(jpa.save(mapper.toEntity(category)));
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
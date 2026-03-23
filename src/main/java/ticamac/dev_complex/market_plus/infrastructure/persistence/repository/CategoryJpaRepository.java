package ticamac.dev_complex.market_plus.infrastructure.persistence.repository;

import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

    List<CategoryEntity> findAllByIsActiveTrue();

    Optional<CategoryEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
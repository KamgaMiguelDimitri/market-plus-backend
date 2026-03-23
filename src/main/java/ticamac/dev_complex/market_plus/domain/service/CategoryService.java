package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.application.dto.catalogue.CategoryRequest;
import ticamac.dev_complex.market_plus.domain.model.Category;
import ticamac.dev_complex.market_plus.domain.port.in.CategoryUseCase;
import ticamac.dev_complex.market_plus.domain.port.out.CategoryRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService implements CategoryUseCase {

    private final CategoryRepositoryPort repository;

    public CategoryService(CategoryRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Category> getTree() {
        List<Category> all = repository.findAllActive();
        return buildTree(all);
    }

    @Override
    public Category getBySlug(String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable : " + slug));
    }

    @Override
    public Category create(CategoryRequest request) {
        String slug = generateSlug(request.getName());
        if (repository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Une catégorie avec ce nom existe déjà.");
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder());
        category.setActive(request.isActive());
        return repository.save(category);
    }

    @Override
    public Category update(UUID id, CategoryRequest request) {
        Category existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable."));
        existing.setName(request.getName());
        existing.setSlug(generateSlug(request.getName()));
        existing.setParentId(request.getParentId());
        existing.setSortOrder(request.getSortOrder());
        existing.setActive(request.isActive());
        return repository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable."));
        repository.deleteById(id);
    }

    private List<Category> buildTree(List<Category> flat) {
        Map<UUID, Category> map = flat.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<Category> roots = new ArrayList<>();
        for (Category c : flat) {
            if (c.getParentId() == null) {
                roots.add(c);
            } else {
                Category parent = map.get(c.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null)
                        parent.setChildren(new ArrayList<>());
                    parent.getChildren().add(c);
                }
            }
        }
        return roots;
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
package ticamac.dev_complex.market_plus.domain.port.in;

import ticamac.dev_complex.market_plus.application.dto.catalogue.CategoryRequest;
import ticamac.dev_complex.market_plus.domain.model.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryUseCase {

    List<Category> getTree();

    Category getBySlug(String slug);

    Category create(CategoryRequest request);

    Category update(UUID id, CategoryRequest request);

    void delete(UUID id);
}
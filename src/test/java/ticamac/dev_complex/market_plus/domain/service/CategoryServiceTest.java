package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.application.dto.catalogue.CategoryRequest;
import ticamac.dev_complex.market_plus.domain.model.Category;
import ticamac.dev_complex.market_plus.domain.port.out.CategoryRepositoryPort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService — tests unitaires")
class CategoryServiceTest {

    @Mock
    private CategoryRepositoryPort repository;
    @InjectMocks
    private CategoryService categoryService;

    // ─── getTree ───────────────────────────────────────────

    @Nested
    @DisplayName("getTree()")
    class GetTreeTests {

        @Test
        @DisplayName("doit construire l'arbre depuis une liste plate")
        void getTree_buildsHierarchy() {
            UUID parentId = UUID.randomUUID();
            UUID childId = UUID.randomUUID();

            Category parent = buildCategory(parentId, "Électronique", null);
            Category child = buildCategory(childId, "Smartphones", parentId);

            when(repository.findAllActive()).thenReturn(List.of(parent, child));

            List<Category> tree = categoryService.getTree();

            assertThat(tree).hasSize(1);
            assertThat(tree.get(0).getName()).isEqualTo("Électronique");
            assertThat(tree.get(0).getChildren()).hasSize(1);
            assertThat(tree.get(0).getChildren().get(0).getName()).isEqualTo("Smartphones");
        }

        @Test
        @DisplayName("doit retourner une liste vide si aucune catégorie")
        void getTree_empty() {
            when(repository.findAllActive()).thenReturn(new ArrayList<>());
            assertThat(categoryService.getTree()).isEmpty();
        }
    }

    // ─── create ────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("doit créer une catégorie avec un slug auto-généré")
        void create_generatesSlug() {
            CategoryRequest request = new CategoryRequest();
            request.setName("Téléphones Mobiles");
            request.setActive(true);

            Category saved = buildCategory(UUID.randomUUID(), "Téléphones Mobiles", null);
            saved.setSlug("tlphones-mobiles");

            when(repository.existsBySlug(anyString())).thenReturn(false);
            when(repository.save(any())).thenReturn(saved);

            Category result = categoryService.create(request);

            assertThat(result).isNotNull();
            verify(repository).save(argThat(c -> c.getSlug() != null && !c.getSlug().isEmpty()));
        }

        @Test
        @DisplayName("doit rejeter si le nom existe déjà")
        void create_duplicateName_throwsException() {
            CategoryRequest request = new CategoryRequest();
            request.setName("Électronique");

            when(repository.existsBySlug(anyString())).thenReturn(true);

            assertThatThrownBy(() -> categoryService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("existe déjà");

            verify(repository, never()).save(any());
        }
    }

    // ─── delete ────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("doit supprimer une catégorie existante")
        void delete_success() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id))
                    .thenReturn(Optional.of(buildCategory(id, "Test", null)));

            categoryService.delete(id);

            verify(repository).deleteById(id);
        }

        @Test
        @DisplayName("doit rejeter si la catégorie est introuvable")
        void delete_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.delete(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("introuvable");

            verify(repository, never()).deleteById(any());
        }
    }

    private Category buildCategory(UUID id, String name, UUID parentId) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setSlug(name.toLowerCase().replaceAll("\\s+", "-"));
        c.setParentId(parentId);
        c.setActive(true);
        return c;
    }
}
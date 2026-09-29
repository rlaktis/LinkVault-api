package com.linkvault.service;

import com.linkvault.domain.Category;
import com.linkvault.dto.CategoryCreateRequest;
import com.linkvault.dto.CategoryResponse;
import com.linkvault.dto.CategoryUpdateRequest;
import com.linkvault.exception.CategoryNotEmptyException;
import com.linkvault.exception.DuplicateResourceException;
import com.linkvault.exception.ResourceNotFoundException;
import com.linkvault.repository.BookmarkRepository;
import com.linkvault.repository.CategoryRepository;
import com.linkvault.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = new Category(categoryId, "Development", "Dev resources", "#2563EB");
    }

    @Test
    @DisplayName("Should return all categories sorted by name")
    void shouldReturnAllCategories() {
        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of(category));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Development");
        verify(categoryRepository).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Should return category by ID when found")
    void shouldReturnCategoryById() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.getCategoryById(categoryId);

        assertThat(result.id()).isEqualTo(categoryId);
        assertThat(result.name()).isEqualTo("Development");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category ID is missing")
    void shouldThrowWhenCategoryNotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    @DisplayName("Should create new category when name is unique")
    void shouldCreateCategorySuccessfully() {
        CategoryCreateRequest request = new CategoryCreateRequest("Design", "Design tools", "#EC4899");

        when(categoryRepository.existsByNameIgnoreCase("Design")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.name()).isEqualTo("Design");
        assertThat(response.color()).isEqualTo("#EC4899");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when category name already exists")
    void shouldThrowOnDuplicateCategoryCreate() {
        CategoryCreateRequest request = new CategoryCreateRequest("Development", null, "#2563EB");
        when(categoryRepository.existsByNameIgnoreCase("Development")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should prevent category deletion when bookmarks exist (RESTRICT rule)")
    void shouldThrowWhenDeletingCategoryWithBookmarks() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(bookmarkRepository.countByCategoryId(categoryId)).thenReturn(5L);

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(CategoryNotEmptyException.class)
                .hasMessageContaining("5 bookmark(s)");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete category when bookmark count is 0")
    void shouldDeleteCategoryWhenEmpty() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(bookmarkRepository.countByCategoryId(categoryId)).thenReturn(0L);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("Should reassign bookmarks to a different category")
    void shouldReassignBookmarks() {
        UUID targetId = UUID.randomUUID();
        Category targetCategory = new Category(targetId, "General", null, "#6B7280");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(targetId)).thenReturn(Optional.of(targetCategory));
        when(bookmarkRepository.reassignCategory(categoryId, targetCategory)).thenReturn(3);

        int count = categoryService.reassignBookmarks(categoryId, targetId);

        assertThat(count).isEqualTo(3);
        verify(bookmarkRepository).reassignCategory(categoryId, targetCategory);
    }
}

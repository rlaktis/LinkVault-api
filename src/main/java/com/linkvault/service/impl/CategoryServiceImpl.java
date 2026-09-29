package com.linkvault.service.impl;

import com.linkvault.domain.Category;
import com.linkvault.dto.CategoryCreateRequest;
import com.linkvault.dto.CategoryResponse;
import com.linkvault.dto.CategoryUpdateRequest;
import com.linkvault.exception.CategoryNotEmptyException;
import com.linkvault.exception.DuplicateResourceException;
import com.linkvault.exception.ResourceNotFoundException;
import com.linkvault.repository.BookmarkRepository;
import com.linkvault.repository.CategoryRepository;
import com.linkvault.service.CategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookmarkRepository bookmarkRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, BookmarkRepository bookmarkRepository) {
        this.categoryRepository = categoryRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        Category category = findCategoryEntityOrThrow(id);
        return CategoryResponse.from(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        String trimmedName = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException(String.format("Category with name '%s' already exists", trimmedName));
        }

        Category category = Category.create(trimmedName, request.description(), request.color().trim());
        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
        Category category = findCategoryEntityOrThrow(id);
        String trimmedName = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new DuplicateResourceException(String.format("Category with name '%s' already exists", trimmedName));
        }

        category.setName(trimmedName);
        category.setDescription(request.description());
        category.setColor(request.color().trim());

        Category updated = categoryRepository.save(category);
        return CategoryResponse.from(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = findCategoryEntityOrThrow(id);

        long bookmarkCount = bookmarkRepository.countByCategoryId(id);
        if (bookmarkCount > 0) {
            throw new CategoryNotEmptyException(id, bookmarkCount);
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public int reassignBookmarks(UUID sourceCategoryId, UUID targetCategoryId) {
        if (sourceCategoryId.equals(targetCategoryId)) {
            throw new IllegalArgumentException("Source and target categories must be different");
        }

        // Verify source category exists
        findCategoryEntityOrThrow(sourceCategoryId);
        // Verify target category exists
        Category targetCategory = findCategoryEntityOrThrow(targetCategoryId);

        return bookmarkRepository.reassignCategory(sourceCategoryId, targetCategory);
    }

    private Category findCategoryEntityOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}

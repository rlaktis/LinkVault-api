package com.linkvault.service;

import com.linkvault.dto.CategoryCreateRequest;
import com.linkvault.dto.CategoryResponse;
import com.linkvault.dto.CategoryUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request);

    void deleteCategory(UUID id);

    int reassignBookmarks(UUID sourceCategoryId, UUID targetCategoryId);
}

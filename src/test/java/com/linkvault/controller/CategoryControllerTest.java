package com.linkvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.dto.CategoryCreateRequest;
import com.linkvault.dto.CategoryResponse;
import com.linkvault.dto.CategoryUpdateRequest;
import com.linkvault.exception.CategoryNotEmptyException;
import com.linkvault.exception.DuplicateResourceException;
import com.linkvault.exception.ResourceNotFoundException;
import com.linkvault.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("GET /api/v1/categories -> 200 OK")
    void shouldReturnAllCategories() throws Exception {
        CategoryResponse response = new CategoryResponse(
                UUID.randomUUID(), "Development", "Dev links", "#3B82F6", Instant.now(), Instant.now()
        );
        when(categoryService.getAllCategories()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Development"))
                .andExpect(jsonPath("$[0].color").value("#3B82F6"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} -> 200 OK when found")
    void shouldReturnCategoryById() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryResponse response = new CategoryResponse(
                id, "Design", "Design tools", "#EC4899", Instant.now(), Instant.now()
        );
        when(categoryService.getCategoryById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Design"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} -> 404 ProblemDetail when not found")
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.getCategoryById(id))
                .thenThrow(new ResourceNotFoundException("Category", id));

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/categories -> 201 Created with Location header")
    void shouldCreateCategory() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryCreateRequest request = new CategoryCreateRequest("Backend", "Backend docs", "#10B981");
        CategoryResponse response = new CategoryResponse(id, "Backend", "Backend docs", "#10B981", Instant.now(), Instant.now());

        when(categoryService.createCategory(any(CategoryCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/categories/" + id))
                .andExpect(jsonPath("$.name").value("Backend"));
    }

    @Test
    @DisplayName("POST /api/v1/categories -> 400 Bad Request on invalid @HexColor")
    void shouldRejectInvalidHexColorOnCreate() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("Backend", "Backend docs", "not-a-hex");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors.color").exists());
    }

    @Test
    @DisplayName("POST /api/v1/categories -> 409 Conflict on duplicate category name")
    void shouldReturn409OnDuplicateName() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("Backend", null, "#10B981");
        when(categoryService.createCategory(any(CategoryCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("Category with name 'Backend' already exists"));

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Resource Conflict"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} -> 200 OK on update")
    void shouldUpdateCategory() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryUpdateRequest request = new CategoryUpdateRequest("Updated Dev", "New desc", "#6366F1");
        CategoryResponse response = new CategoryResponse(id, "Updated Dev", "New desc", "#6366F1", Instant.now(), Instant.now());

        when(categoryService.updateCategory(eq(id), any(CategoryUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Dev"));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} -> 204 No Content when empty")
    void shouldDeleteCategory() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(categoryService).deleteCategory(id);

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} -> 409 Conflict when category contains bookmarks")
    void shouldRejectDeleteWhenBookmarksExist() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new CategoryNotEmptyException(id, 3L)).when(categoryService).deleteCategory(id);

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Category Deletion Restricted"))
                .andExpect(jsonPath("$.bookmarkCount").value(3));
    }

    @Test
    @DisplayName("PATCH /api/v1/categories/{sourceId}/reassign-bookmarks -> 200 OK")
    void shouldReassignBookmarks() throws Exception {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(categoryService.reassignBookmarks(sourceId, targetId)).thenReturn(5);

        mockMvc.perform(patch("/api/v1/categories/{sourceId}/reassign-bookmarks", sourceId)
                        .param("targetCategoryId", targetId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reassignedCount").value(5));
    }
}

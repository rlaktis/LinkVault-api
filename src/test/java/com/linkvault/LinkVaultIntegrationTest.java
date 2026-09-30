package com.linkvault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.dto.BookmarkCreateRequest;
import com.linkvault.dto.CategoryCreateRequest;
import com.linkvault.dto.CategoryResponse;
import com.linkvault.repository.BookmarkRepository;
import com.linkvault.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LinkVaultIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private BookmarkRepository bookmarkRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @BeforeEach
        void setUp() {
                bookmarkRepository.deleteAll();
                categoryRepository.deleteAll();
        }

        @Test
        @DisplayName("E2E Workflow: Category CRUD, Bookmark Lifecycle, Constraint Enforcement, and Cleanup")
        void testFullEndToEndLifecycle() throws Exception {
                // 1. Create a Category
                CategoryCreateRequest categoryRequest = new CategoryCreateRequest(
                                "Cloud Architecture",
                                "Articles and references for cloud systems",
                                "#3B82F6");

                MvcResult categoryResult = mockMvc.perform(post("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(header().exists("Location"))
                                .andExpect(jsonPath("$.id").isNotEmpty())
                                .andExpect(jsonPath("$.name").value("Cloud Architecture"))
                                .andExpect(jsonPath("$.color").value("#3B82F6"))
                                .andReturn();

                CategoryResponse createdCategory = objectMapper.readValue(
                                categoryResult.getResponse().getContentAsString(),
                                CategoryResponse.class);
                UUID categoryId = createdCategory.id();

                // 2. Create a Bookmark under this Category
                BookmarkCreateRequest bookmarkRequest = new BookmarkCreateRequest(
                                "AWS Well-Architected Framework",
                                "https://aws.amazon.com/architecture/well-architected",
                                "Pillars of cloud excellence",
                                true,
                                categoryId);

                MvcResult bookmarkResult = mockMvc.perform(post("/api/v1/bookmarks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bookmarkRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(header().exists("Location"))
                                .andExpect(jsonPath("$.id").isNotEmpty())
                                .andExpect(jsonPath("$.title").value("AWS Well-Architected Framework"))
                                .andExpect(jsonPath("$.category.id").value(categoryId.toString()))
                                .andExpect(jsonPath("$.isFavorite").value(true))
                                .andReturn();

                String location = bookmarkResult.getResponse().getHeader("Location");
                assertThat(location).isNotNull();
                String bookmarkIdStr = location.substring(location.lastIndexOf('/') + 1);
                UUID bookmarkId = UUID.fromString(bookmarkIdStr);

                // 3. Verify Category details
                mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Cloud Architecture"))
                                .andExpect(jsonPath("$.color").value("#3B82F6"));

                // 4. Attempt to delete non-empty Category -> expect 409 Conflict
                // (CategoryNotEmptyException via RFC 7807)
                mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.title").value("Category Deletion Restricted"))
                                .andExpect(jsonPath("$.status").value(409));

                // 5. Toggle favorite status on the bookmark
                mockMvc.perform(patch("/api/v1/bookmarks/{id}/favorite", bookmarkId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isFavorite").value(false));

                // 6. Delete the bookmark -> expect 204 No Content
                mockMvc.perform(delete("/api/v1/bookmarks/{id}", bookmarkId))
                                .andExpect(status().isNoContent());

                // 7. Delete the category now that it's empty -> expect 204 No Content
                mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                                .andExpect(status().isNoContent());

                // 8. Verify both repositories are empty
                assertThat(bookmarkRepository.count()).isZero();
                assertThat(categoryRepository.count()).isZero();
        }
}

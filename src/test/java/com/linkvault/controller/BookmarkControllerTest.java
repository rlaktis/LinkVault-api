package com.linkvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.dto.BookmarkCreateRequest;
import com.linkvault.dto.BookmarkResponse;
import com.linkvault.dto.BookmarkUpdateRequest;
import com.linkvault.dto.CategorySummaryResponse;
import com.linkvault.exception.ResourceNotFoundException;
import com.linkvault.service.BookmarkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookmarkController.class)
class BookmarkControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private BookmarkService bookmarkService;

        @Test
        @DisplayName("GET /api/v1/bookmarks -> 200 OK with paginated list")
        void shouldReturnBookmarksPage() throws Exception {
                UUID categoryId = UUID.randomUUID();
                CategorySummaryResponse categorySummary = new CategorySummaryResponse(categoryId, "Development",
                                "#3B82F6");
                BookmarkResponse bookmarkResponse = new BookmarkResponse(
                                UUID.randomUUID(), "Spring Docs", "https://spring.io", "Official docs", true,
                                categorySummary, Instant.now(), Instant.now());

                when(bookmarkService.getBookmarks(any(), any(), any(), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(bookmarkResponse)));

                mockMvc.perform(get("/api/v1/bookmarks"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].title").value("Spring Docs"))
                                .andExpect(jsonPath("$.content[0].category.name").value("Development"));
        }

        @Test
        @DisplayName("GET /api/v1/bookmarks/{id} -> 200 OK when found")
        void shouldReturnBookmarkById() throws Exception {
                UUID id = UUID.randomUUID();
                CategorySummaryResponse categorySummary = new CategorySummaryResponse(UUID.randomUUID(), "Development",
                                "#3B82F6");
                BookmarkResponse response = new BookmarkResponse(
                                id, "PostgreSQL", "https://postgresql.org", null, false, categorySummary, Instant.now(),
                                Instant.now());

                when(bookmarkService.getBookmarkById(id)).thenReturn(response);

                mockMvc.perform(get("/api/v1/bookmarks/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(id.toString()))
                                .andExpect(jsonPath("$.url").value("https://postgresql.org"));
        }

        @Test
        @DisplayName("GET /api/v1/bookmarks/{id} -> 404 Not Found when missing")
        void shouldReturn404WhenMissing() throws Exception {
                UUID id = UUID.randomUUID();
                when(bookmarkService.getBookmarkById(id)).thenThrow(new ResourceNotFoundException("Bookmark", id));

                mockMvc.perform(get("/api/v1/bookmarks/{id}", id))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource Not Found"));
        }

        @Test
        @DisplayName("POST /api/v1/bookmarks -> 201 Created with Location header")
        void shouldCreateBookmark() throws Exception {
                UUID bookmarkId = UUID.randomUUID();
                UUID categoryId = UUID.randomUUID();
                BookmarkCreateRequest request = new BookmarkCreateRequest("Spring Boot", "https://spring.io",
                                "Great framework", true, categoryId);
                CategorySummaryResponse categorySummary = new CategorySummaryResponse(categoryId, "Dev", "#2563EB");
                BookmarkResponse response = new BookmarkResponse(
                                bookmarkId, "Spring Boot", "https://spring.io", "Great framework", true,
                                categorySummary, Instant.now(), Instant.now());

                when(bookmarkService.createBookmark(any(BookmarkCreateRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/bookmarks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", "/api/v1/bookmarks/" + bookmarkId))
                                .andExpect(jsonPath("$.title").value("Spring Boot"));
        }

        @Test
        @DisplayName("POST /api/v1/bookmarks -> 400 Bad Request on invalid @ValidUrl")
        void shouldRejectInvalidUrlOnCreate() throws Exception {
                BookmarkCreateRequest request = new BookmarkCreateRequest("Bad URL", "ftp://invalid.com", null, false,
                                UUID.randomUUID());

                mockMvc.perform(post("/api/v1/bookmarks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Bad Request"))
                                .andExpect(jsonPath("$.errors.url").exists());
        }

        @Test
        @DisplayName("PUT /api/v1/bookmarks/{id} -> 200 OK on update")
        void shouldUpdateBookmark() throws Exception {
                UUID bookmarkId = UUID.randomUUID();
                UUID categoryId = UUID.randomUUID();
                BookmarkUpdateRequest request = new BookmarkUpdateRequest("Updated Title", "https://spring.io/updated",
                                "Updated", false, categoryId);
                CategorySummaryResponse categorySummary = new CategorySummaryResponse(categoryId, "Dev", "#2563EB");
                BookmarkResponse response = new BookmarkResponse(
                                bookmarkId, "Updated Title", "https://spring.io/updated", "Updated", false,
                                categorySummary, Instant.now(), Instant.now());

                when(bookmarkService.updateBookmark(eq(bookmarkId), any(BookmarkUpdateRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(put("/api/v1/bookmarks/{id}", bookmarkId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Updated Title"));
        }

        @Test
        @DisplayName("PATCH /api/v1/bookmarks/{id}/favorite -> 200 OK toggles favorite")
        void shouldToggleFavorite() throws Exception {
                UUID bookmarkId = UUID.randomUUID();
                CategorySummaryResponse categorySummary = new CategorySummaryResponse(UUID.randomUUID(), "Dev",
                                "#2563EB");
                BookmarkResponse response = new BookmarkResponse(
                                bookmarkId, "Title", "https://example.com", null, true, categorySummary, Instant.now(),
                                Instant.now());

                when(bookmarkService.toggleFavorite(bookmarkId)).thenReturn(response);

                mockMvc.perform(patch("/api/v1/bookmarks/{id}/favorite", bookmarkId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isFavorite").value(true));
        }

        @Test
        @DisplayName("DELETE /api/v1/bookmarks/{id} -> 204 No Content")
        void shouldDeleteBookmark() throws Exception {
                UUID id = UUID.randomUUID();
                doNothing().when(bookmarkService).deleteBookmark(id);

                mockMvc.perform(delete("/api/v1/bookmarks/{id}", id))
                                .andExpect(status().isNoContent());
        }
}

package com.linkvault.service;

import com.linkvault.domain.Bookmark;
import com.linkvault.domain.Category;
import com.linkvault.dto.BookmarkCreateRequest;
import com.linkvault.dto.BookmarkResponse;
import com.linkvault.dto.BookmarkUpdateRequest;
import com.linkvault.exception.ResourceNotFoundException;
import com.linkvault.repository.BookmarkRepository;
import com.linkvault.repository.CategoryRepository;
import com.linkvault.service.impl.BookmarkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    private Category category;
    private Bookmark bookmark;
    private UUID bookmarkId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        bookmarkId = UUID.randomUUID();
        category = new Category(categoryId, "Development", "Dev resources", "#2563EB");
        bookmark = new Bookmark(bookmarkId, "Spring Docs", "https://spring.io", "Official reference", false, category);
    }

    @Test
    @DisplayName("Should return paginated bookmarks with category info")
    void shouldReturnBookmarksPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookmarkRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(bookmark)));

        Page<BookmarkResponse> result = bookmarkService.getBookmarks(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Spring Docs");
        assertThat(result.getContent().get(0).category().name()).isEqualTo("Development");
    }

    @Test
    @DisplayName("Should filter bookmarks by categoryId and favorite flag")
    void shouldFilterByCategoryAndFavorite() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookmarkRepository.findByCategoryIdAndIsFavoriteTrue(categoryId, pageable))
                .thenReturn(new PageImpl<>(List.of(bookmark)));

        Page<BookmarkResponse> result = bookmarkService.getBookmarks(categoryId, true, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(bookmarkRepository).findByCategoryIdAndIsFavoriteTrue(categoryId, pageable);
    }

    @Test
    @DisplayName("Should search bookmarks when search keyword provided")
    void shouldSearchBookmarks() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookmarkRepository.searchByTitleOrUrl("spring", pageable))
                .thenReturn(new PageImpl<>(List.of(bookmark)));

        Page<BookmarkResponse> result = bookmarkService.getBookmarks(null, null, "spring", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(bookmarkRepository).searchByTitleOrUrl("spring", pageable);
    }

    @Test
    @DisplayName("Should create bookmark with valid category association")
    void shouldCreateBookmark() {
        BookmarkCreateRequest request = new BookmarkCreateRequest("PostgreSQL Docs", "https://postgresql.org",
                "DB docs", true, categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(inv -> inv.getArgument(0));

        BookmarkResponse response = bookmarkService.createBookmark(request);

        assertThat(response.title()).isEqualTo("PostgreSQL Docs");
        assertThat(response.url()).isEqualTo("https://postgresql.org");
        assertThat(response.isFavorite()).isTrue();
        assertThat(response.category().name()).isEqualTo("Development");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating bookmark with invalid category")
    void shouldThrowOnInvalidCategoryWhenCreating() {
        UUID nonExistingCategoryId = UUID.randomUUID();
        BookmarkCreateRequest request = new BookmarkCreateRequest("PostgreSQL", "https://postgresql.org", null, false,
                nonExistingCategoryId);

        when(categoryRepository.findById(nonExistingCategoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookmarkService.createBookmark(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");

        verify(bookmarkRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should toggle favorite flag on bookmark")
    void shouldToggleFavorite() {
        assertThat(bookmark.isFavorite()).isFalse();

        when(bookmarkRepository.findByIdWithCategory(bookmarkId)).thenReturn(Optional.of(bookmark));
        when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(inv -> inv.getArgument(0));

        BookmarkResponse response = bookmarkService.toggleFavorite(bookmarkId);

        assertThat(response.isFavorite()).isTrue();
    }

    @Test
    @DisplayName("Should delete bookmark when ID exists")
    void shouldDeleteBookmark() {
        when(bookmarkRepository.findById(bookmarkId)).thenReturn(Optional.of(bookmark));

        bookmarkService.deleteBookmark(bookmarkId);

        verify(bookmarkRepository).delete(bookmark);
    }
}

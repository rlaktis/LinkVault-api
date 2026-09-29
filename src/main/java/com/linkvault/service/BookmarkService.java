package com.linkvault.service;

import com.linkvault.dto.BookmarkCreateRequest;
import com.linkvault.dto.BookmarkResponse;
import com.linkvault.dto.BookmarkUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookmarkService {

    Page<BookmarkResponse> getBookmarks(UUID categoryId, Boolean favorite, String search, Pageable pageable);

    BookmarkResponse getBookmarkById(UUID id);

    BookmarkResponse createBookmark(BookmarkCreateRequest request);

    BookmarkResponse updateBookmark(UUID id, BookmarkUpdateRequest request);

    BookmarkResponse toggleFavorite(UUID id);

    void deleteBookmark(UUID id);
}

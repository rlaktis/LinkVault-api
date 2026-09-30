package com.linkvault.controller;

import com.linkvault.dto.BookmarkCreateRequest;
import com.linkvault.dto.BookmarkResponse;
import com.linkvault.dto.BookmarkUpdateRequest;
import com.linkvault.service.BookmarkService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller managing Bookmark endpoints.
 */
@RestController
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public ResponseEntity<Page<BookmarkResponse>> getBookmarks(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BookmarkResponse> bookmarks = bookmarkService.getBookmarks(categoryId, favorite, search, pageable);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookmarkResponse> getBookmarkById(@PathVariable UUID id) {
        BookmarkResponse bookmark = bookmarkService.getBookmarkById(id);
        return ResponseEntity.ok(bookmark);
    }

    @PostMapping
    public ResponseEntity<BookmarkResponse> createBookmark(@Valid @RequestBody BookmarkCreateRequest request) {
        BookmarkResponse created = bookmarkService.createBookmark(request);
        URI location = URI.create("/api/v1/bookmarks/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookmarkResponse> updateBookmark(
            @PathVariable UUID id,
            @Valid @RequestBody BookmarkUpdateRequest request) {
        BookmarkResponse updated = bookmarkService.updateBookmark(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<BookmarkResponse> toggleFavorite(@PathVariable UUID id) {
        BookmarkResponse updated = bookmarkService.toggleFavorite(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable UUID id) {
        bookmarkService.deleteBookmark(id);
        return ResponseEntity.noContent().build();
    }
}

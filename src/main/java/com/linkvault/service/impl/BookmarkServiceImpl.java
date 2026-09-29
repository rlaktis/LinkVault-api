package com.linkvault.service.impl;

import com.linkvault.domain.Bookmark;
import com.linkvault.domain.Category;
import com.linkvault.dto.BookmarkCreateRequest;
import com.linkvault.dto.BookmarkResponse;
import com.linkvault.dto.BookmarkUpdateRequest;
import com.linkvault.exception.ResourceNotFoundException;
import com.linkvault.repository.BookmarkRepository;
import com.linkvault.repository.CategoryRepository;
import com.linkvault.service.BookmarkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final CategoryRepository categoryRepository;

    public BookmarkServiceImpl(BookmarkRepository bookmarkRepository, CategoryRepository categoryRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<BookmarkResponse> getBookmarks(UUID categoryId, Boolean favorite, String search, Pageable pageable) {
        Page<Bookmark> bookmarksPage;

        if (search != null && !search.trim().isEmpty()) {
            bookmarksPage = bookmarkRepository.searchByTitleOrUrl(search.trim(), pageable);
        } else if (categoryId != null && Boolean.TRUE.equals(favorite)) {
            bookmarksPage = bookmarkRepository.findByCategoryIdAndIsFavoriteTrue(categoryId, pageable);
        } else if (categoryId != null) {
            bookmarksPage = bookmarkRepository.findByCategoryId(categoryId, pageable);
        } else if (Boolean.TRUE.equals(favorite)) {
            bookmarksPage = bookmarkRepository.findByIsFavoriteTrue(pageable);
        } else {
            bookmarksPage = bookmarkRepository.findAll(pageable);
        }

        return bookmarksPage.map(BookmarkResponse::from);
    }

    @Override
    public BookmarkResponse getBookmarkById(UUID id) {
        Bookmark bookmark = bookmarkRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", id));
        return BookmarkResponse.from(bookmark);
    }

    @Override
    @Transactional
    public BookmarkResponse createBookmark(BookmarkCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        Bookmark bookmark = Bookmark.create(
                request.title().trim(),
                request.url().trim(),
                request.description(),
                request.favoriteOrDefault(),
                category);

        Bookmark saved = bookmarkRepository.save(bookmark);
        return BookmarkResponse.from(saved);
    }

    @Override
    @Transactional
    public BookmarkResponse updateBookmark(UUID id, BookmarkUpdateRequest request) {
        Bookmark bookmark = bookmarkRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", id));

        if (!bookmark.getCategory().getId().equals(request.categoryId())) {
            Category newCategory = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
            bookmark.setCategory(newCategory);
        }

        bookmark.setTitle(request.title().trim());
        bookmark.setUrl(request.url().trim());
        bookmark.setDescription(request.description());
        if (request.isFavorite() != null) {
            bookmark.setFavorite(request.isFavorite());
        }

        Bookmark updated = bookmarkRepository.save(bookmark);
        return BookmarkResponse.from(updated);
    }

    @Override
    @Transactional
    public BookmarkResponse toggleFavorite(UUID id) {
        Bookmark bookmark = bookmarkRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", id));

        bookmark.setFavorite(!bookmark.isFavorite());
        Bookmark saved = bookmarkRepository.save(bookmark);
        return BookmarkResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteBookmark(UUID id) {
        Bookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", id));
        bookmarkRepository.delete(bookmark);
    }
}

package com.linkvault.dto;

import com.linkvault.domain.Bookmark;

import java.time.Instant;
import java.util.UUID;

/**
 * Full Bookmark response DTO containing embedded CategorySummaryResponse.
 */
public record BookmarkResponse(
        UUID id,
        String title,
        String url,
        String description,
        boolean isFavorite,
        CategorySummaryResponse category,
        Instant createdAt,
        Instant updatedAt) {
    public static BookmarkResponse from(Bookmark bookmark) {
        if (bookmark == null) {
            return null;
        }
        return new BookmarkResponse(
                bookmark.getId(),
                bookmark.getTitle(),
                bookmark.getUrl(),
                bookmark.getDescription(),
                bookmark.isFavorite(),
                CategorySummaryResponse.from(bookmark.getCategory()),
                bookmark.getCreatedAt(),
                bookmark.getUpdatedAt());
    }
}

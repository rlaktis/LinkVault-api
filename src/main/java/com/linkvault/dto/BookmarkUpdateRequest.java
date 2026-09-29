package com.linkvault.dto;

import com.linkvault.validation.ValidUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request payload for updating a Bookmark.
 */
public record BookmarkUpdateRequest(
        @NotBlank(message = "Bookmark title must not be blank") @Size(min = 1, max = 200, message = "Bookmark title must be between 1 and 200 characters") String title,

        @NotBlank(message = "Bookmark URL must not be blank") @Size(max = 2048, message = "Bookmark URL must not exceed 2048 characters") @ValidUrl String url,

        @Size(max = 1000, message = "Bookmark description must not exceed 1000 characters") String description,

        Boolean isFavorite,

        @NotNull(message = "Category ID must not be null") UUID categoryId) {
    public boolean favoriteOrDefault() {
        return Boolean.TRUE.equals(isFavorite);
    }
}

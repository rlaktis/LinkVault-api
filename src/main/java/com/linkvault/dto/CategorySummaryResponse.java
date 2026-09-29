package com.linkvault.dto;

import com.linkvault.domain.Category;

import java.util.UUID;

/**
 * Compact Category summary embedded inside Bookmark responses.
 */
public record CategorySummaryResponse(
        UUID id,
        String name,
        String color) {
    public static CategorySummaryResponse from(Category category) {
        if (category == null) {
            return null;
        }
        return new CategorySummaryResponse(
                category.getId(),
                category.getName(),
                category.getColor());
    }
}

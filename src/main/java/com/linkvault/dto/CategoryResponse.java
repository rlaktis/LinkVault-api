package com.linkvault.dto;

import com.linkvault.domain.Category;

import java.time.Instant;
import java.util.UUID;

/**
 * Full Category response DTO.
 */
public record CategoryResponse(
        UUID id,
        String name,
        String description,
        String color,
        Instant createdAt,
        Instant updatedAt) {
    public static CategoryResponse from(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getColor(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}

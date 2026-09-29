package com.linkvault.dto;

import com.linkvault.validation.HexColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating a Category.
 */
public record CategoryUpdateRequest(
                @NotBlank(message = "Category name must not be blank") @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters") String name,

                @Size(max = 255, message = "Category description must not exceed 255 characters") String description,

                @NotBlank(message = "Category color must not be blank") @HexColor String color) {
}

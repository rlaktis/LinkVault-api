package com.linkvault.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class HexColorValidatorTest {

    private final HexColorValidator validator = new HexColorValidator();

    @ParameterizedTest
    @ValueSource(strings = { "#3B82F6", "#FFFFFF", "#000000", "#abcdef", "#123456" })
    @DisplayName("Should accept valid 6-digit hex colors")
    void shouldAcceptValidHexColors(String color) {
        assertThat(validator.isValid(color, null)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("Should return true for null or empty (delegated to @NotBlank)")
    void shouldAcceptNullOrEmpty(String color) {
        assertThat(validator.isValid(color, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "3B82F6", "#FFF", "#GGGGGG", "#12345", "#1234567", "red", "blue", "#12 456" })
    @DisplayName("Should reject invalid hex color formats")
    void shouldRejectInvalidHexColors(String color) {
        assertThat(validator.isValid(color, null)).isFalse();
    }
}

package com.linkvault.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValidUrlValidatorTest {

    private final ValidUrlValidator validator = new ValidUrlValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "https://spring.io",
            "http://localhost:8080",
            "https://docs.spring.io/spring-boot/docs/current/reference/html/",
            "https://github.com/OvatTheLegend/LinkVault-api?tab=readme-ov-file#overview",
            "http://example.com/path?param=1&other=2"
    })
    @DisplayName("Should accept valid HTTP/HTTPS URLs")
    void shouldAcceptValidUrls(String url) {
        assertThat(validator.isValid(url, null)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("Should return true for null or empty (delegated to @NotBlank)")
    void shouldAcceptNullOrEmpty(String url) {
        assertThat(validator.isValid(url, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ftp://ftp.example.com",
            "javascript:alert(1)",
            "file:///C:/Users/test",
            "not-a-url",
            "http://",
            "https://",
            "://invalid"
    })
    @DisplayName("Should reject invalid or non-HTTP/HTTPS URLs")
    void shouldRejectInvalidUrls(String url) {
        assertThat(validator.isValid(url, null)).isFalse();
    }
}

package com.linkvault.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;

/**
 * ConstraintValidator implementing URL validation.
 * Verifies that the string is an absolute URI with an http/https scheme and a
 * non-empty host.
 */
public class ValidUrlValidator implements ConstraintValidator<ValidUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty checks
        }

        try {
            URI uri = URI.create(value.trim());
            if (!uri.isAbsolute()) {
                return false;
            }

            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }

            String host = uri.getHost();
            return host != null && !host.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}

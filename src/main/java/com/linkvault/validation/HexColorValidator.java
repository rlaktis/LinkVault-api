package com.linkvault.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * ConstraintValidator implementing hex color format validation.
 */
public class HexColorValidator implements ConstraintValidator<HexColor, String> {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6})$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty checks
        }
        return HEX_COLOR_PATTERN.matcher(value.trim()).matches();
    }
}

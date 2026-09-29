package com.linkvault.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string is a valid 6-character hex color format (e.g. #3B82F6
 * or #ffffff).
 */
@Documented
@Constraint(validatedBy = HexColorValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface HexColor {

    String message() default "must be a valid 6-digit hex color code (e.g. #3B82F6)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package com.linkvault.exception;

/**
 * Thrown when attempting to create or update a resource with a unique
 * constraint violation (e.g. duplicate category name).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

package com.linkvault.exception;

/**
 * Thrown when a requested entity (Category or Bookmark) is not found in the
 * database.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("%s with identifier '%s' was not found", resourceName, identifier));
    }
}

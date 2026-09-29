package com.linkvault.exception;

import java.util.UUID;

/**
 * Thrown when attempting to delete a Category that still contains Bookmarks.
 */
public class CategoryNotEmptyException extends RuntimeException {

    private final UUID categoryId;
    private final long bookmarkCount;

    public CategoryNotEmptyException(UUID categoryId, long bookmarkCount) {
        super(String.format(
                "Cannot delete category '%s' because it still contains %d bookmark(s). Reassign or delete them first.",
                categoryId, bookmarkCount));
        this.categoryId = categoryId;
        this.bookmarkCount = bookmarkCount;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public long getBookmarkCount() {
        return bookmarkCount;
    }
}

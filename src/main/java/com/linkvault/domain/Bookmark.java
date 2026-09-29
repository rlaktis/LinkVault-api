package com.linkvault.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Bookmark (saved link) belonging to a specific Category.
 */
@Entity
@Table(name = "bookmarks")
public class Bookmark extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "is_favorite", nullable = false)
    private boolean isFavorite = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected Bookmark() {
        // JPA required no-arg constructor
    }

    public Bookmark(UUID id, String title, String url, String description, boolean isFavorite, Category category) {
        this.id = id != null ? id : UUID.randomUUID();
        this.title = Objects.requireNonNull(title, "Bookmark title must not be null");
        this.url = Objects.requireNonNull(url, "Bookmark url must not be null");
        this.description = description;
        this.isFavorite = isFavorite;
        this.category = Objects.requireNonNull(category, "Category reference must not be null");
    }

    public static Bookmark create(String title, String url, String description, boolean isFavorite, Category category) {
        return new Bookmark(UUID.randomUUID(), title, url, description, isFavorite, category);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category, "Category must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Bookmark bookmark))
            return false;
        return id != null && id.equals(bookmark.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }
}

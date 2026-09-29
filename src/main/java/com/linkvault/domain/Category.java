package com.linkvault.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Category folder used to organize bookmarks.
 */
@Entity
@Table(name = "categories")
public class Category extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "color", nullable = false, length = 7)
    private String color;

    protected Category() {
        // JPA required no-arg constructor
    }

    public Category(UUID id, String name, String description, String color) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = Objects.requireNonNull(name, "Category name must not be null");
        this.description = description;
        this.color = Objects.requireNonNull(color, "Category color must not be null");
    }

    public static Category create(String name, String description, String color) {
        return new Category(UUID.randomUUID(), name, description, color);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Category category))
            return false;
        return id != null && id.equals(category.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}

package com.linkvault.repository;

import com.linkvault.domain.Bookmark;
import com.linkvault.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Bookmark entities.
 * Uses @EntityGraph on list/paged queries to fetch associated Category eagerly in 1 single SQL query,
 * eliminating the N+1 select problem.
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    /**
     * Finds a single bookmark by ID with its Category eagerly fetched.
     */
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT b FROM Bookmark b WHERE b.id = :id")
    Optional<Bookmark> findByIdWithCategory(@Param("id") UUID id);

    /**
     * Finds all bookmarks with pagination, eagerly joining the Category.
     */
    @Override
    @EntityGraph(attributePaths = {"category"})
    Page<Bookmark> findAll(Pageable pageable);

    /**
     * Finds bookmarks belonging to a specific Category with pagination.
     */
    @EntityGraph(attributePaths = {"category"})
    Page<Bookmark> findByCategoryId(UUID categoryId, Pageable pageable);

    /**
     * Finds favorite bookmarks with pagination.
     */
    @EntityGraph(attributePaths = {"category"})
    Page<Bookmark> findByIsFavoriteTrue(Pageable pageable);

    /**
     * Finds favorite bookmarks in a specific Category with pagination.
     */
    @EntityGraph(attributePaths = {"category"})
    Page<Bookmark> findByCategoryIdAndIsFavoriteTrue(UUID categoryId, Pageable pageable);

    /**
     * Search bookmarks by title or URL (case-insensitive) with pagination.
     */
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT b FROM Bookmark b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.url) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Bookmark> searchByTitleOrUrl(@Param("query") String query, Pageable pageable);

    /**
     * Counts how many bookmarks belong to a given Category ID.
     * Used for domain deletion-restriction invariants.
     */
    long countByCategoryId(UUID categoryId);

    /**
     * Bulk reassigns all bookmarks from one category to another in a single atomic SQL UPDATE.
     */
    @Modifying
    @Query("UPDATE Bookmark b SET b.category = :targetCategory WHERE b.category.id = :sourceCategoryId")
    int reassignCategory(@Param("sourceCategoryId") UUID sourceCategoryId, @Param("targetCategory") Category targetCategory);
}

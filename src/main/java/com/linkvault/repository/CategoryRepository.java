package com.linkvault.repository;

import com.linkvault.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Category entities.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Checks if a category with the given name already exists (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Checks if a category with the given name exists excluding a specific ID (for
     * update validation).
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    /**
     * Finds a category by its name (case-insensitive).
     */
    Optional<Category> findByNameIgnoreCase(String name);
}

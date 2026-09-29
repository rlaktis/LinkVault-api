package com.linkvault.repository;

import com.linkvault.domain.Bookmark;
import com.linkvault.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category devCategory;
    private Category designCategory;

    @BeforeEach
    void setUp() {
        bookmarkRepository.deleteAll();
        categoryRepository.deleteAll();

        devCategory = new Category(UUID.randomUUID(), "Development", "Dev resources", "#2563EB");
        designCategory = new Category(UUID.randomUUID(), "Design", "Design tools", "#EC4899");

        entityManager.persistAndFlush(devCategory);
        entityManager.persistAndFlush(designCategory);
    }

    @Test
    @DisplayName("Should verify CategoryRepository case-insensitive uniqueness checks")
    void testCategoryExistenceChecks() {
        assertThat(categoryRepository.existsByNameIgnoreCase("development")).isTrue();
        assertThat(categoryRepository.existsByNameIgnoreCase("DEVELOPMENT")).isTrue();
        assertThat(categoryRepository.existsByNameIgnoreCase("NonExisting")).isFalse();

        assertThat(categoryRepository.existsByNameIgnoreCaseAndIdNot("Development", devCategory.getId())).isFalse();
        assertThat(categoryRepository.existsByNameIgnoreCaseAndIdNot("Development", designCategory.getId())).isTrue();
    }

    @Test
    @DisplayName("Should fetch bookmarks with eager category and verify countByCategoryId")
    void testBookmarkQueriesAndEntityGraph() {
        Bookmark bookmark1 = Bookmark.create("Spring Boot Docs", "https://spring.io/projects/spring-boot", "Official docs", true, devCategory);
        Bookmark bookmark2 = Bookmark.create("PostgreSQL Docs", "https://postgresql.org/docs", "DB docs", false, devCategory);
        Bookmark bookmark3 = Bookmark.create("Figma", "https://figma.com", "UI design tool", true, designCategory);

        entityManager.persist(bookmark1);
        entityManager.persist(bookmark2);
        entityManager.persist(bookmark3);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to test real SQL queries

        // Test count by category
        assertThat(bookmarkRepository.countByCategoryId(devCategory.getId())).isEqualTo(2);
        assertThat(bookmarkRepository.countByCategoryId(designCategory.getId())).isEqualTo(1);

        // Test findByIdWithCategory
        Optional<Bookmark> fetched = bookmarkRepository.findByIdWithCategory(bookmark1.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getTitle()).isEqualTo("Spring Boot Docs");
        assertThat(fetched.get().getCategory().getName()).isEqualTo("Development");

        // Test pagination and favorite filtering
        Page<Bookmark> favorites = bookmarkRepository.findByIsFavoriteTrue(PageRequest.of(0, 10));
        assertThat(favorites.getTotalElements()).isEqualTo(2);

        // Test search query
        Page<Bookmark> searchResult = bookmarkRepository.searchByTitleOrUrl("spring", PageRequest.of(0, 10));
        assertThat(searchResult.getTotalElements()).isEqualTo(1);
        assertThat(searchResult.getContent().get(0).getTitle()).isEqualTo("Spring Boot Docs");
    }

    @Test
    @DisplayName("Should atomically reassign bookmarks from one category to another")
    void testReassignCategory() {
        Bookmark bookmark1 = Bookmark.create("Spring Docs", "https://spring.io", null, false, devCategory);
        Bookmark bookmark2 = Bookmark.create("Java Docs", "https://docs.oracle.com", null, false, devCategory);

        entityManager.persist(bookmark1);
        entityManager.persist(bookmark2);
        entityManager.flush();
        entityManager.clear();

        assertThat(bookmarkRepository.countByCategoryId(devCategory.getId())).isEqualTo(2);
        assertThat(bookmarkRepository.countByCategoryId(designCategory.getId())).isEqualTo(0);

        // Reassign all bookmarks from devCategory to designCategory
        int updatedCount = bookmarkRepository.reassignCategory(devCategory.getId(), designCategory);
        assertThat(updatedCount).isEqualTo(2);

        entityManager.clear();

        assertThat(bookmarkRepository.countByCategoryId(devCategory.getId())).isEqualTo(0);
        assertThat(bookmarkRepository.countByCategoryId(designCategory.getId())).isEqualTo(2);
    }
}

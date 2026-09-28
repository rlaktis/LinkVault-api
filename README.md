# 🔖 LinkVault — Relational Bookmarks & Categories API

LinkVault is a robust, production-grade RESTful API for managing web bookmarks organized into categories. Built with **Spring Boot 3.3**, **Java 21**, **PostgreSQL 16**, and **Spring Data JPA & Hibernate 6**.

---

## 🏛️ Database Design & Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    CATEGORIES ||--o{ BOOKMARKS : "1 Category has 0 to Many Bookmarks"
    
    CATEGORIES {
        UUID id PK "Primary Key"
        VARCHAR_50 name UK "Unique, Not Null"
        VARCHAR_255 description "Nullable"
        VARCHAR_7 color "Hex Code (e.g. #3B82F6)"
        TIMESTAMP created_at "Audit timestamp"
        TIMESTAMP updated_at "Audit timestamp"
    }

    BOOKMARKS {
        UUID id PK "Primary Key"
        VARCHAR_200 title "Bookmark title"
        VARCHAR_2048 url "Validated URL target"
        VARCHAR_1000 description "Nullable notes"
        BOOLEAN is_favorite "Default FALSE"
        UUID category_id FK "References categories(id)"
        TIMESTAMP created_at "Audit timestamp"
        TIMESTAMP updated_at "Audit timestamp"
    }
```

---

## 🛠️ Tech Stack & Architecture

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.3.x
- **Persistence:** Spring Data JPA / Hibernate 6
- **Database:** PostgreSQL 16 (Docker Compose)
- **Migrations:** Flyway (Versioned DDL)
- **Connection Pool:** HikariCP
- **Validation:** Jakarta Validation with Custom Annotations (`@ValidUrl`, `@HexColor`)
- **Error Handling:** RFC 7807 `ProblemDetail`
- **Testing Pyramid:** JUnit 5, Mockito, AssertJ, `@DataJpaTest`, `@WebMvcTest`, Testcontainers

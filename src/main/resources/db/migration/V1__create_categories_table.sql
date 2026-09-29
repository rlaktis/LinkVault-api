CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    color VARCHAR(7) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_categories_name UNIQUE (name)
);

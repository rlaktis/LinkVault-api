CREATE TABLE bookmarks (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    description VARCHAR(1000),
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    category_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_bookmarks_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT
);

CREATE INDEX idx_bookmarks_category_id ON bookmarks (category_id);
CREATE INDEX idx_bookmarks_is_favorite ON bookmarks (is_favorite);

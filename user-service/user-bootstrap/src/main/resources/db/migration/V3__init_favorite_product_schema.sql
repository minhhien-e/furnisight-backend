-- Favorite product state is owned by user-service.
CREATE TABLE favorite_products
(
    id         UUID      NOT NULL PRIMARY KEY,
    account_id UUID      NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    product_id UUID      NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_favorite_products_account_product UNIQUE (account_id, product_id)
);

CREATE INDEX idx_favorite_products_account_id ON favorite_products (account_id);
CREATE INDEX idx_favorite_products_product_id ON favorite_products (product_id);

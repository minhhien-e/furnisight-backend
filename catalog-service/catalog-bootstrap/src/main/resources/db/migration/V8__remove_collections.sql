ALTER TABLE products
    DROP COLUMN IF EXISTS collection_id;

DROP TABLE IF EXISTS collections;

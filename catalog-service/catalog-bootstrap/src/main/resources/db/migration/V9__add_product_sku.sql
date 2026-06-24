ALTER TABLE products
ADD COLUMN IF NOT EXISTS sku VARCHAR(255);

UPDATE products
SET sku = slug
WHERE sku IS NULL OR sku = '';

ALTER TABLE products
ADD CONSTRAINT products_sku_unique UNIQUE (sku);

CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);

ALTER TABLE product_variants
ADD COLUMN IF NOT EXISTS sku VARCHAR(100);

UPDATE product_variants
SET sku = id::text
WHERE sku IS NULL OR sku = '';

CREATE INDEX IF NOT EXISTS idx_product_variants_sku ON product_variants(sku);

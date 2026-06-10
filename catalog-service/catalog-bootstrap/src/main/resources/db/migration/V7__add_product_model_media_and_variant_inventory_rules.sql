ALTER TABLE products
    ADD COLUMN IF NOT EXISTS model_media_id UUID;

ALTER TABLE product_variants
    ADD COLUMN IF NOT EXISTS low_stock_threshold INTEGER NOT NULL DEFAULT 5;

UPDATE product_variants
SET sku = UPPER(TRIM(sku));

UPDATE product_variants
SET sku = UPPER(id::text)
WHERE sku IS NULL OR sku = '';

WITH duplicates AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY sku ORDER BY id) AS duplicate_number
    FROM product_variants
)
UPDATE product_variants variant
SET sku = variant.sku || '-' || LEFT(variant.id::text, 8)
FROM duplicates
WHERE variant.id = duplicates.id
  AND duplicates.duplicate_number > 1;

DROP INDEX IF EXISTS idx_product_variants_sku;

ALTER TABLE product_variants
    ALTER COLUMN sku SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_product_variants_sku
    ON product_variants (UPPER(sku));

ALTER TABLE product_variants
    ADD CONSTRAINT chk_product_variants_sku_not_blank CHECK (TRIM(sku) <> '');

ALTER TABLE product_variants
    ADD CONSTRAINT chk_product_variants_low_stock_threshold
        CHECK (low_stock_threshold BETWEEN 1 AND 9999);

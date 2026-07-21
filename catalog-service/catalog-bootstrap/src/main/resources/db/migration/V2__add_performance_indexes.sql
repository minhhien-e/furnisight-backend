-- ============================================================
-- V2__add_performance_indexes.sql
-- Add missing indexes to improve search and read performance
-- ============================================================

-- Foreign key indexes (crucial for JOINs and subqueries)
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX IF NOT EXISTS idx_product_images_product_id ON product_images(product_id);

-- Filter and sort indexes
CREATE INDEX IF NOT EXISTS idx_products_status ON products(product_status);
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_products_sold_count ON products(sold_count DESC);
CREATE INDEX IF NOT EXISTS idx_products_rating ON products(rating DESC);

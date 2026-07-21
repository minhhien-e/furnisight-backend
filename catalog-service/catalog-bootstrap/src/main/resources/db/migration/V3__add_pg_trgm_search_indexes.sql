-- ============================================================
-- V3__add_pg_trgm_search_indexes.sql
-- Add GIN indexes with pg_trgm for ultra-fast LIKE '%...%' text search
-- ============================================================

-- Enable the pg_trgm extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create GIN indexes for the columns used in searchProducts (LIKE queries)
CREATE INDEX IF NOT EXISTS idx_products_name_trgm ON products USING gin (LOWER(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_desc_trgm ON products USING gin (LOWER(description) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_slug_trgm ON products USING gin (LOWER(slug) gin_trgm_ops);

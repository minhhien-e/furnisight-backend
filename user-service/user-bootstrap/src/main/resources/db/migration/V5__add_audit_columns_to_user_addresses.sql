-- ============================================================
-- V5__add_audit_columns_to_user_addresses.sql
-- Add created_at and updated_at to user_addresses to match BaseEntity
-- ============================================================

ALTER TABLE user_addresses
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

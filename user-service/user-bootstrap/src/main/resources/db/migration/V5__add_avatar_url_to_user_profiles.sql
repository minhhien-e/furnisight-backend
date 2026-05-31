-- ============================================================
-- V5__add_avatar_url_to_user_profiles.sql
-- Add avatar_url column to store OAuth2 provider avatar URLs
-- (used as fallback when avatar_media_id is not set)
-- ============================================================

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(2048);

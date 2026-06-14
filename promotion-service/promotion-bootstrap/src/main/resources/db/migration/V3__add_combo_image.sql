ALTER TABLE promotion_combos
    ADD COLUMN IF NOT EXISTS image_media_id UUID,
    ADD COLUMN IF NOT EXISTS image_url TEXT;

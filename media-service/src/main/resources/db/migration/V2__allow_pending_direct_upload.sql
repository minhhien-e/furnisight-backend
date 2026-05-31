ALTER TABLE media_assets
    ALTER COLUMN cloudinary_public_id DROP NOT NULL,
    ALTER COLUMN url DROP NOT NULL,
    ALTER COLUMN secure_url DROP NOT NULL;

package com.furnisight.media.core.repository;

import com.furnisight.media.core.model.entity.MediaVariant;
import java.util.UUID;

public interface MediaVariantRepository {
    MediaVariant findById(UUID id);
    MediaVariant save(MediaVariant variant);
}

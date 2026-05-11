package com.furnisight.notification.application.media.port.out.repository;

import com.furnisight.notification.application.media.dto.projection.MediaProjection;

import java.util.UUID;

public interface MediaRepository {
    MediaProjection getById(UUID mediaId);
}

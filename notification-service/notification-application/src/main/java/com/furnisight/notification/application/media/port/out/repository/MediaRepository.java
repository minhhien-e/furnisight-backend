package com.furnisight.notification.application.media.port.out.repository;

import com.furnisight.notification.application.media.dto.response.MediaResponse;

import java.util.UUID;

public interface MediaRepository {
    MediaResponse getById(UUID mediaId);
}

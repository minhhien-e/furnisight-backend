package com.furnisight.notification.application.profile.port.out.repository;

import com.furnisight.notification.domain.model.entity.NotificationProfile;

import java.util.Optional;
import java.util.UUID;

public interface NotificationProfileRepository {
    NotificationProfile save(NotificationProfile entity);
    Optional<NotificationProfile> findById(UUID id);
    void deleteById(UUID id);
    NotificationProfile findByUserId(UUID userId);
    String getEmailByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}

package com.furnisight.notification.application.notification.port.out.repository;

import com.furnisight.notification.domain.model.entity.NotificationLog;

import java.util.Optional;
import java.util.UUID;

public interface NotificationLogRepository {
    NotificationLog save(NotificationLog entity);
    Optional<NotificationLog> findById(UUID id);
    void deleteById(UUID id);
}

package com.furnisight.notification.application.template.port.out.repository;

import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository {
    NotificationTemplate save(NotificationTemplate entity);
    Optional<NotificationTemplate> findById(UUID id);
    void deleteById(UUID id);
    NotificationTemplate findByCode(String code);

    List<NotificationTemplate> filter(String name, NotificationChannel channel, NotificationType type);
}

package com.furnisight.notification.adapter.out.repository.jpa;

import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateJpaRepository extends MongoRepository<NotificationTemplate, UUID> {
    Optional<NotificationTemplate> findByCode(String code);
}

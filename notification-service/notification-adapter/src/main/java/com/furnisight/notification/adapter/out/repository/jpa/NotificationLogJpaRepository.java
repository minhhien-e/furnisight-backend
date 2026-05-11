package com.furnisight.notification.adapter.out.repository.jpa;

import com.furnisight.notification.domain.model.entity.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationLogJpaRepository extends MongoRepository<NotificationLog, UUID> {
}

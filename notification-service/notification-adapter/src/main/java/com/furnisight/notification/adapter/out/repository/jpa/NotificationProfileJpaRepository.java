package com.furnisight.notification.adapter.out.repository.jpa;

import com.furnisight.notification.domain.model.entity.NotificationProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationProfileJpaRepository extends MongoRepository<NotificationProfile, UUID> {
    Optional<NotificationProfile> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    @Query(value = "{ 'userId': ?0 }", fields = "{ 'userEmail': 1, '_id': 0 }")
    String getEmailByUserId(UUID userId);
}

package com.furnisight.notification.adapter.out.repository.jpa.impl;

import com.furnisight.notification.adapter.out.repository.jpa.NotificationProfileJpaRepository;
import com.furnisight.notification.application.profile.port.out.repository.NotificationProfileRepository;
import com.furnisight.notification.domain.model.entity.NotificationProfile;
import com.furnisight.notification.domain.exception.NotificationProfileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationProfileRepositoryImpl implements NotificationProfileRepository {

    private final NotificationProfileJpaRepository jpaRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public NotificationProfile save(NotificationProfile notificationProfile) {
        return jpaRepository.save(notificationProfile);
    }

    @Override
    public Optional<NotificationProfile> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public NotificationProfile findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId)
            .orElseThrow(() -> new NotificationProfileNotFoundException("userId", userId));
    }

    @Override
    public String getEmailByUserId(UUID userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.fields().include("userEmail").exclude("_id");
        Document doc = mongoTemplate.findOne(query, Document.class, "notification_profiles");
        if (doc == null) return null;
        return doc.getString("userEmail");
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}

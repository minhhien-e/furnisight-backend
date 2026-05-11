package com.furnisight.notification.adapter.out.repository.jpa.impl;

import com.furnisight.notification.adapter.out.repository.jpa.NotificationTemplateJpaRepository;
import com.furnisight.notification.application.template.port.out.repository.NotificationTemplateRepository;
import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import com.furnisight.notification.domain.exception.NotificationTemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import com.furnisight.notification.domain.model.enums.NotificationType;
import com.furnisight.notification.domain.model.enums.NotificationChannel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationTemplateRepositoryImpl implements NotificationTemplateRepository {

    private final NotificationTemplateJpaRepository jpaRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public NotificationTemplate save(NotificationTemplate notificationTemplate) {
        return jpaRepository.save(notificationTemplate);
    }

    @Override
    public Optional<NotificationTemplate> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public NotificationTemplate findByCode(String code) {
        return jpaRepository.findByCode(code)
                .orElseThrow(() -> new NotificationTemplateNotFoundException("code", code));
    }

    @Override
    public List<NotificationTemplate> filter(String name, NotificationChannel channel, NotificationType type) {
        Query query = new Query();
        if (StringUtils.hasText(name)) {
            query.addCriteria(Criteria.where("name").regex(name, "i"));
        }
        if (channel != null) {
            query.addCriteria(Criteria.where("channel").is(channel));
        }
        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        return mongoTemplate.find(query, NotificationTemplate.class);
    }
}

package com.furnisight.notification.adapter.out.repository.jpa.impl;

import com.furnisight.notification.adapter.out.repository.jpa.OutboxEventJpaRepository;
import com.furnisight.notification.application.outbox.port.out.repository.OutboxEventRepository;
import com.furnisight.notification.domain.model.entity.OutboxEvent;
import com.furnisight.notification.domain.model.enums.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final OutboxEventJpaRepository jpaRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return jpaRepository.save(outboxEvent);
    }

    @Override
    public List<OutboxEvent> saveAll(List<OutboxEvent> outboxEvents) {
        return jpaRepository.saveAll(outboxEvents);
    }

    @Override
    public List<OutboxEvent> findAllByStatus(OutboxStatus status, int limit) {
        Query query = new Query(Criteria.where("status").is(status))
                .with(Sort.by(Sort.Direction.ASC, "createdAt"))
                .limit(limit);
        return mongoTemplate.find(query, OutboxEvent.class);
    }
}

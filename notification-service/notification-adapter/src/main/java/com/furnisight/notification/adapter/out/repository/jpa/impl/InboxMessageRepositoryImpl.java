package com.furnisight.notification.adapter.out.repository.jpa.impl;

import com.furnisight.notification.adapter.out.repository.jpa.InboxMessageJpaRepository;
import com.furnisight.notification.application.inbox.port.out.repository.InboxMessageRepository;
import com.furnisight.notification.domain.model.entity.InboxMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InboxMessageRepositoryImpl implements InboxMessageRepository {

    private final InboxMessageJpaRepository jpaRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public InboxMessage save(InboxMessage inboxMessage) {
        return jpaRepository.save(inboxMessage);
    }

    @Override
    public Optional<InboxMessage> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void markAllAsReadByUserId(UUID userId) {
        Query query = new Query(Criteria.where("userId").is(userId).and("read").is(false));
        Update update = new Update()
            .set("read", true)
            .set("readAt", java.time.LocalDateTime.now());
        mongoTemplate.updateMulti(query, update, InboxMessage.class);
    }

    @Override
    public List<InboxMessage> findAllByUserId(UUID userId, boolean deleted, LocalDateTime startDate, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return jpaRepository.findMessages(userId, deleted, startDate, pageable);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

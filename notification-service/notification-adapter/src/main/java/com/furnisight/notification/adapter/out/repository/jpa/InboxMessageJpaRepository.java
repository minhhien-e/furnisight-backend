package com.furnisight.notification.adapter.out.repository.jpa;

import com.furnisight.notification.domain.model.entity.InboxMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InboxMessageJpaRepository extends MongoRepository<InboxMessage, UUID> {
    @Query(value = "{ 'userId': ?0, 'deleted': ?1, 'createdAt': { $gte: ?2 } }")
    List<InboxMessage> findMessages(UUID userId, boolean deleted, LocalDateTime startDate, Pageable pageable);}

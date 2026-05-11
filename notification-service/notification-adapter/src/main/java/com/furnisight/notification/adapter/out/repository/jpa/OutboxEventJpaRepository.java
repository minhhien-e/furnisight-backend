package com.furnisight.notification.adapter.out.repository.jpa;

import com.furnisight.notification.domain.model.entity.OutboxEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutboxEventJpaRepository extends MongoRepository<OutboxEvent, UUID> {
}

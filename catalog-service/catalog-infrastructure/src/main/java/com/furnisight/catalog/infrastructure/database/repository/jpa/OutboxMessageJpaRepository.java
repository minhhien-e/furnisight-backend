package com.furnisight.catalog.infrastructure.database.repository.jpa;

import com.furnisight.catalog.domain.entities.OutboxMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxMessageJpaRepository extends JpaRepository<OutboxMessage, UUID> {

    @Query("SELECT o FROM OutboxMessage o " +
           "WHERE o.processedAt IS NULL " +
           "AND o.failed = false " +
           "AND (o.nextRetryAt IS NULL OR o.nextRetryAt <= CURRENT_TIMESTAMP) " +
           "ORDER BY o.createdAt ASC")
    List<OutboxMessage> findPendingMessages(Pageable pageable);
}

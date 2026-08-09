package com.furnisight.order.adapter.out.repository.jpa;

import com.furnisight.order.domain.entities.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

@Repository
public interface OutboxMessageJpaRepository extends JpaRepository<OutboxMessage, UUID> {
    
    @Query("SELECT o FROM OutboxMessage o WHERE (o.processedAt IS NULL AND o.failed = false) OR (o.processedAt IS NULL AND o.failed = false AND o.nextRetryAt <= CURRENT_TIMESTAMP) ORDER BY o.createdAt ASC")
    List<OutboxMessage> findPendingMessages(Pageable pageable);
}

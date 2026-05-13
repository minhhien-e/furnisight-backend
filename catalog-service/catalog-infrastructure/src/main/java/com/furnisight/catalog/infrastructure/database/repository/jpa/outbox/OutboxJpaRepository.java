package com.furnisight.catalog.infrastructure.database.repository.jpa.outbox;

import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity.OutboxMessage;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxJpaRepository extends JpaRepository<OutboxMessage, UUID> {
    List<OutboxMessage> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Modifying
    @Query("DELETE FROM OutboxMessage m WHERE m.status = :status AND m.processedAt < :cutoffTime")
    int deleteByStatusAndProcessedAtBefore(@Param("status") OutboxStatus status, @Param("cutoffTime") java.time.LocalDateTime cutoffTime);
}


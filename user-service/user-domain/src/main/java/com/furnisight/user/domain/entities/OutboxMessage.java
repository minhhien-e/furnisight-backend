package com.furnisight.user.domain.entities;

import com.furnisight.user.domain.seedwork.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessage extends AggregateRoot {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "failed", nullable = false)
    private boolean failed = false;

    public OutboxMessage(String aggregateType, String aggregateId, String type, String payload) {
        this.id = UUID.randomUUID();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsProcessed() {
        this.processedAt = LocalDateTime.now();
        this.errorMessage = null;
        this.nextRetryAt = null;
        this.failed = false;
    }

    public void markAsFailed(String errorMessage) {
        this.errorMessage = errorMessage;
        this.retryCount++;
        if (this.retryCount >= 3) {
            this.failed = true;
        } else {
            long delayMinutes = (long) Math.pow(5, this.retryCount - 1) * 2;
            this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
        }
    }
}

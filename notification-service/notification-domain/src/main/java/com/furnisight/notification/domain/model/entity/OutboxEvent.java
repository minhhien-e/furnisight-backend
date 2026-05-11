package com.furnisight.notification.domain.model.entity;

import com.furnisight.notification.domain.model.enums.OutboxStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "outbox_events")
@CompoundIndexes({
    @CompoundIndex(name = "status_createdAt_idx", def = "{'status': 1, 'createdAt': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    private UUID id;

    private String aggregateType;
    private String aggregateId;

    private String eventType;
    private String payload;

    private OutboxStatus status;

    private int retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}

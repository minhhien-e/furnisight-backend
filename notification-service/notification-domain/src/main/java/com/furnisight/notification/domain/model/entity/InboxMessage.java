package com.furnisight.notification.domain.model.entity;

import com.furnisight.notification.domain.event.NotificationCreatedEvent;
import com.furnisight.notification.domain.model.base.DomainEntity;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.furnisight.notification.domain.model.enums.NotificationType;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "inbox_messages")
@Data
@NoArgsConstructor
@Builder
public class InboxMessage extends DomainEntity {

    @Id
    private UUID id;
    private UUID userId;

    private String title;
    private String body;
    private String image;
    private String actionUrl;
    private NotificationType type;
    private boolean read;
    private boolean deleted;
    private LocalDateTime readAt;

    private LocalDateTime deletedAt;
    @Indexed(expireAfter = "30d")
    private LocalDateTime expireAt;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @Version
    private Long version;

    public InboxMessage(UUID id, UUID userId, String title, String body, String image, String actionUrl, NotificationType type, boolean read, boolean deleted, LocalDateTime readAt, LocalDateTime deletedAt, LocalDateTime expireAt, LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.image = image;
        this.actionUrl = actionUrl;
        this.type = type;
        this.read = read;
        this.deleted = deleted;
        this.readAt = readAt;
        this.deletedAt = deletedAt;
        this.expireAt = expireAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
        addDomainEvent(NotificationCreatedEvent.builder()
            .title(title)
            .body(body)
            .actionUrl(actionUrl)
            .type(type)
            .userId(userId)
            .image(image)
            .build());
    }
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}

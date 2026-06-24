package com.furnisight.notification.application.inbox.port.in.dto.response;

import com.furnisight.notification.domain.model.entity.InboxMessage;
import com.furnisight.notification.domain.model.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@lombok.Data
@lombok.Builder
public class InboxMessageResponse {
    private UUID id;
    private String title;
    private String body;
    private String image;
    private String actionUrl;
    private NotificationType type;

    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private Map<String, Object> metadata;

    public static InboxMessageResponse from(InboxMessage inboxMessage) {
        return InboxMessageResponse.builder()
            .id(inboxMessage.getId())
            .title(inboxMessage.getTitle())
            .body(inboxMessage.getBody())
            .image(inboxMessage.getImage())
            .actionUrl(inboxMessage.getActionUrl())
            .type(inboxMessage.getType())
            .isRead(inboxMessage.isRead())
            .readAt(inboxMessage.getReadAt())
            .deletedAt(inboxMessage.getDeletedAt())
            .createdAt(inboxMessage.getCreatedAt())
            .metadata(inboxMessage.getMetadata())
            .build();
    }

}

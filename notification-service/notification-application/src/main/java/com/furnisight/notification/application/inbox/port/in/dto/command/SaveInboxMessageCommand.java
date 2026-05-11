package com.furnisight.notification.application.inbox.port.in.dto.command;

import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveInboxMessageCommand {
    private UUID userId;
    private String title;
    private String body;
    private String image;
    private String actionUrl;
    private NotificationType type;
}

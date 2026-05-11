package com.furnisight.notification.domain.model.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.furnisight.notification.domain.model.enums.NotificationType;
import com.furnisight.notification.domain.model.enums.NotificationChannel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "notification_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate {

    @Id
    private UUID id;
    private String code;
    private String name;
    private List<String> variables;
    private String titleTemplate;
    private String bodyTemplate;
    private NotificationType type;
    private NotificationChannel channel;
    private String defaultImage;
    private String defaultActionUrl;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @Version
    private Long version;
}

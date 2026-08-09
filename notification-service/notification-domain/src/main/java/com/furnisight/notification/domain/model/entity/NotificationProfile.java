package com.furnisight.notification.domain.model.entity;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.furnisight.notification.domain.model.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notification_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationProfile {

    @Id
    private UUID id;
    private UUID userId;

    private boolean orderUpdatesEnabled;
    private boolean promotionsEnabled;
    private boolean walletUpdatesEnabled;
    private boolean socialUpdatesEnabled;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @Version
    private Long version;

    public boolean canReceiveNotificationType(NotificationType type) {
        if (type == null) return true;

        return switch (type) {
            case ORDER -> this.orderUpdatesEnabled;
            case PROMOTION -> this.promotionsEnabled;
            case WALLET -> this.walletUpdatesEnabled;
            case SOCIAL, REVIEW -> this.socialUpdatesEnabled;
            case MEDIA, SYSTEM -> true;
        };
    }
}

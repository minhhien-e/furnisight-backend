package com.furnisight.notification.application.profile.port.in.dto.projection;

import com.furnisight.notification.domain.model.entity.NotificationProfile;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationProfileProjection {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private boolean orderUpdatesEnabled;
    private boolean promotionsEnabled;
    private boolean walletUpdatesEnabled;
    private boolean socialUpdatesEnabled;
    public static NotificationProfileProjection from(NotificationProfile preference) {
        return NotificationProfileProjection.builder()
            .id(preference.getId())
            .userId(preference.getUserId())
            .userEmail(preference.getUserEmail())
            .orderUpdatesEnabled(preference.isOrderUpdatesEnabled())
            .promotionsEnabled(preference.isPromotionsEnabled())
            .walletUpdatesEnabled(preference.isWalletUpdatesEnabled())
            .socialUpdatesEnabled(preference.isSocialUpdatesEnabled())
            .build();
    }
}

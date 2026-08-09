package com.furnisight.notification.application.profile.port.in.dto.response;

import com.furnisight.notification.domain.model.entity.NotificationProfile;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationProfileResponse {
    private UUID id;
    private UUID userId;
    private boolean orderUpdatesEnabled;
    private boolean promotionsEnabled;
    private boolean walletUpdatesEnabled;
    private boolean socialUpdatesEnabled;

    public static NotificationProfileResponse from(NotificationProfile preference) {
        return NotificationProfileResponse.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .orderUpdatesEnabled(preference.isOrderUpdatesEnabled())
                .promotionsEnabled(preference.isPromotionsEnabled())
                .walletUpdatesEnabled(preference.isWalletUpdatesEnabled())
                .socialUpdatesEnabled(preference.isSocialUpdatesEnabled())
                .build();
    }
}

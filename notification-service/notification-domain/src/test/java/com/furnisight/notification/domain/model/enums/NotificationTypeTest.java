package com.furnisight.notification.domain.model.enums;

import com.furnisight.notification.domain.model.entity.NotificationProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTypeTest {

    @Test
    void supportsPersistedReviewNotifications() {
        assertThat(NotificationType.valueOf("REVIEW")).isEqualTo(NotificationType.REVIEW);
    }

    @Test
    void reviewNotificationsFollowSocialPreference() {
        NotificationProfile disabled = NotificationProfile.builder()
                .socialUpdatesEnabled(false)
                .build();
        NotificationProfile enabled = NotificationProfile.builder()
                .socialUpdatesEnabled(true)
                .build();

        assertThat(disabled.canReceiveNotificationType(NotificationType.REVIEW)).isFalse();
        assertThat(enabled.canReceiveNotificationType(NotificationType.REVIEW)).isTrue();
    }
}

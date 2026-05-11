package com.furnisight.notification.domain.exception;

import com.furnisight.notification.domain.exception.enums.ErrorCode;
import com.furnisight.notification.domain.model.enums.NotificationType;

import java.util.UUID;

public class NotificationProfileNotAllowedException extends BaseException {

    public NotificationProfileNotAllowedException(UUID userId, NotificationType type) {
        super(ErrorCode.NOTIFICATION_PREFERENCE_NOT_ALLOWED, "Notification type " + type + " is not allowed for user: " + userId, null);
    }
}

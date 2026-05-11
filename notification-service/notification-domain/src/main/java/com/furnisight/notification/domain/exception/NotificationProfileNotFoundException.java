package com.furnisight.notification.domain.exception;

import com.furnisight.notification.domain.exception.enums.ErrorCode;

import java.util.UUID;

public class NotificationProfileNotFoundException extends BaseException {

    public NotificationProfileNotFoundException(UUID id) {
        super(ErrorCode.NOTIFICATION_PREFERENCE_NOT_FOUND, "Notification preference not found with id: " + id, null);
    }

    public NotificationProfileNotFoundException(String fieldName, Object value) {
        super(ErrorCode.NOTIFICATION_PREFERENCE_NOT_FOUND, "Notification preference not found with " + fieldName + ": " + value, null);
    }
}

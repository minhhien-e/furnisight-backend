package com.furnisight.notification.domain.exception;

import com.furnisight.notification.domain.exception.enums.ErrorCode;

import java.util.UUID;

public class NotificationLogNotFoundException extends BaseException {

    public NotificationLogNotFoundException(UUID id) {
        super(ErrorCode.NOTIFICATION_LOG_NOT_FOUND, "Notification log not found with id: " + id, null);
    }
}

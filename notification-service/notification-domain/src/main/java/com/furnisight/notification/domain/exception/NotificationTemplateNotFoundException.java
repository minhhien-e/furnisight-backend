package com.furnisight.notification.domain.exception;

import com.furnisight.notification.domain.exception.enums.ErrorCode;

import java.util.UUID;

public class NotificationTemplateNotFoundException extends BaseException {

    public NotificationTemplateNotFoundException(UUID id) {
        super(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND, "Notification template not found with id: " + id, null);
    }

    public NotificationTemplateNotFoundException(String fieldName, Object value) {
        super(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND, "Notification template not found with " + fieldName + ": " + value, null);
    }
}

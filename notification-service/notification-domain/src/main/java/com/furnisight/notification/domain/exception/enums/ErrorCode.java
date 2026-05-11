package com.furnisight.notification.domain.exception.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    NOT_FOUND("NOT_FOUND", "Resource not found"),
    INBOX_MESSAGE_NOT_FOUND("INBOX_MESSAGE_NOT_FOUND", "Inbox message not found"),
    NOTIFICATION_LOG_NOT_FOUND("NOTIFICATION_LOG_NOT_FOUND", "Notification log not found"),
    NOTIFICATION_PREFERENCE_NOT_FOUND("NOTIFICATION_PREFERENCE_NOT_FOUND", "Notification preference not found"),
    NOTIFICATION_TEMPLATE_NOT_FOUND("NOTIFICATION_TEMPLATE_NOT_FOUND", "Notification template not found"),
    TEMPLATE_NAME_REQUIRED("TEMPLATE_NAME_REQUIRED", "Template name cannot be null or empty"),
    TEMPLATE_NAME_TOO_LONG("TEMPLATE_NAME_TOO_LONG", "Template name exceeds maximum length"),
    TEMPLATE_TITLE_REQUIRED("TEMPLATE_TITLE_REQUIRED", "Template title cannot be null or empty"),
    TEMPLATE_TITLE_TOO_LONG("TEMPLATE_TITLE_TOO_LONG", "Template title exceeds maximum length"),
    TEMPLATE_BODY_REQUIRED("TEMPLATE_BODY_REQUIRED", "Template body cannot be null or empty"),
    TEMPLATE_BODY_TOO_LONG("TEMPLATE_BODY_TOO_LONG", "Template body exceeds maximum length"),
    NOTIFICATION_PREFERENCE_NOT_ALLOWED("NOTIFICATION_PREFERENCE_NOT_ALLOWED", "Notification preference is not allowed"),
    UNSUPPORTED_NOTIFICATION_CHANNEL("UNSUPPORTED_NOTIFICATION_CHANNEL", "Unsupported notification channel");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

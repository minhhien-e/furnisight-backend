package com.furniro.MessageService.util.error;

import lombok.Getter;

@Getter
public enum NotificationErrorCode {
    NOTIFICATION_FAILED(400, "Notification failed"),
    NOTIFICATION_NOT_FOUND(404, "Notification not found"),
    NOTIFICATION_ALREADY_EXISTS(409, "Notification already exists"),
    NOTIFICATION_DELETE_FAILED(400, "Notification delete failed"),
    NOTIFICATION_UPDATE_FAILED(400, "Notification update failed");

    private final int code;
    private final String message;

    NotificationErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

package com.furniro.MessageService.util.error;

import lombok.Getter;

@Getter
public enum MessageErrorCode {
    MESSAGE_FAILED(400, "Message failed"),
    MESSAGE_NOT_FOUND(404, "Message not found"),
    MESSAGE_ALREADY_EXISTS(409, "Message already exists"),
    MESSAGE_DELETE_FAILED(400, "Message delete failed"),
    MESSAGE_UPDATE_FAILED(400, "Message update failed");

    private final int code;
    private final String message;

    MessageErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

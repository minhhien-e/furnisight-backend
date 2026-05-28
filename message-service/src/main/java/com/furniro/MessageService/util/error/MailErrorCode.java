package com.furniro.MessageService.util.error;

import lombok.Getter;

@Getter
public enum MailErrorCode {
    MAIL_FAILED(400, "Mail failed"),
    MAIL_NOT_FOUND(404, "Mail not found"),
    MAIL_ALREADY_EXISTS(409, "Mail already exists"),
    MAIL_DELETE_FAILED(400, "Mail delete failed"),
    MAIL_UPDATE_FAILED(400, "Mail update failed");

    private final int code;
    private final String message;

    MailErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
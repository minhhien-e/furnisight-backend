package com.furnisight.notification.domain.exception;

import com.furnisight.notification.domain.exception.enums.ErrorCode;

import java.util.UUID;

public class InboxMessageNotFoundException extends BaseException {

    public InboxMessageNotFoundException(UUID id) {
        super(ErrorCode.INBOX_MESSAGE_NOT_FOUND, "Inbox message not found with id: " + id, null);
    }
}

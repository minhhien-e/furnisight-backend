package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Collections;

public class OutboxEventProcessingException extends BaseException {
    public OutboxEventProcessingException(String message, Throwable cause) {
        super(ErrorCode.OUTBOX_EVENT_PROCESSING_FAILED, message, Collections.emptyMap());
        this.initCause(cause);
    }
}

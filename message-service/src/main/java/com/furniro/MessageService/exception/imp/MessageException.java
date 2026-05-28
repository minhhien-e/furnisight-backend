package com.furniro.MessageService.exception.imp;

import com.furniro.MessageService.exception.BaseException;
import com.furniro.MessageService.util.error.MessageErrorCode;

import lombok.Getter;

@Getter
public class MessageException extends BaseException {
        private final MessageErrorCode errorCode;

    public MessageException(MessageErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

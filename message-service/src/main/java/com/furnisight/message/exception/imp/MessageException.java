package com.furnisight.message.exception.imp;

import com.furnisight.message.exception.BaseException;
import com.furnisight.message.util.error.MessageErrorCode;

import lombok.Getter;

@Getter
public class MessageException extends BaseException {
        private final MessageErrorCode errorCode;

    public MessageException(MessageErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

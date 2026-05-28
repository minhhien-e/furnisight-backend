package com.furniro.MessageService.exception.imp;

import com.furniro.MessageService.exception.BaseException;
import com.furniro.MessageService.util.error.MailErrorCode;

import lombok.Getter;

@Getter
public class MailException extends BaseException {
    private final MailErrorCode errorCode;

    public MailException(MailErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

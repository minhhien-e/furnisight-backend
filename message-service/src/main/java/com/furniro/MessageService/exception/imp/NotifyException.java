package com.furniro.MessageService.exception.imp;

import com.furniro.MessageService.exception.BaseException;
import com.furniro.MessageService.util.error.NotificationErrorCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotifyException extends BaseException {
    private final NotificationErrorCode errorCode;

    public NotifyException(NotificationErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

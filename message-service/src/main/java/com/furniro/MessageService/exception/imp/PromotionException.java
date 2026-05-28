package com.furniro.MessageService.exception.imp;

import com.furniro.MessageService.exception.BaseException;
import com.furniro.MessageService.util.error.PromotionErrorCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionException extends BaseException {
    private final PromotionErrorCode errorCode;

    public PromotionException(PromotionErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

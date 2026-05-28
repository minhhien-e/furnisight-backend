package com.furniro.MessageService.util.error;

import lombok.Getter;

@Getter
public enum PromotionErrorCode {
    PROMOTION_NOT_FOUND(404, "Promotion not found");

    private final int code;
    private final String message;

    PromotionErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}

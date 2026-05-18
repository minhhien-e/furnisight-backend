package com.furnisight.review.core.exception;

import com.furnisight.review.core.exception.enums.ErrorCode;
import lombok.Getter;
import java.util.Map;

@Getter
public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> attributes;

    protected BaseException(ErrorCode errorCode, String message, Map<String, Object> attributes) {
        super(message);
        this.errorCode = errorCode;
        this.attributes = attributes;
    }
}

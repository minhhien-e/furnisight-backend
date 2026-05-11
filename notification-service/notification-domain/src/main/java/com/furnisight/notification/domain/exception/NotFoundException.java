package com.furnisight.notification.domain.exception;

import com.furnisight.notification.domain.exception.enums.ErrorCode;
import java.util.Map;

public class NotFoundException extends BaseException {
    
    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message, null);
    }

    public NotFoundException(String message, Map<String, Object> attributes) {
        super(ErrorCode.NOT_FOUND, message, attributes);
    }
}

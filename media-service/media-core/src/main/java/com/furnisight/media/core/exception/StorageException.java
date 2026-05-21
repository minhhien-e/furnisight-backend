package com.furnisight.media.core.exception;

import com.furnisight.media.core.exception.enums.ErrorCode;

import java.util.Map;

public class StorageException extends BaseException {
    public StorageException(String message) {
        super(ErrorCode.STORAGE_ERROR, message, Map.of());
    }

    public StorageException(ErrorCode errorCode, String message, Map<String, Object> attributes) {
        super(errorCode, message, attributes);
    }
}

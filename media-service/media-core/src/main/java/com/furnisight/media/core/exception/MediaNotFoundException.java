package com.furnisight.media.core.exception;

import com.furnisight.media.core.exception.enums.ErrorCode;

import java.util.Map;

public class MediaNotFoundException extends BaseException {
    public MediaNotFoundException(String file) {
        super(ErrorCode.MEDIA_NOT_FOUND, "File not found: " + file, Map.of("file", file));
    }
}

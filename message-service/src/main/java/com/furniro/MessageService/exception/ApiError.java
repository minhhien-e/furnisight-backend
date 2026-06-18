package com.furniro.MessageService.exception;

import java.time.LocalDateTime;

public record ApiError(
    LocalDateTime timestamp,
    int status,
    String error,
    String code,
    String message,
    String path
) {
}

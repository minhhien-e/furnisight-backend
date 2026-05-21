package com.furnisight.catalog.presentation.web.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
}

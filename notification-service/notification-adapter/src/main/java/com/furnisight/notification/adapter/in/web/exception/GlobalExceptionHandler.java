package com.furnisight.notification.adapter.in.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.furnisight.notification.domain.exceptions.*;
import io.grpc.StatusRuntimeException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: message={}, path={}", message, request.getRequestURI());

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument: message={}, path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected exception occurred at path={}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred", request.getRequestURI());
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }


    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String code, String message, String path) {
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(apiError);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(
            DomainException ex,
            HttpServletRequest request
    ) {
        String code = "BAD_REQUEST";
        if (ex.getErrorCode() != null) {
            code = ex.getErrorCode().name();
        }
        log.warn("Domain exception occurred: code={}, message={}, path={}", code, ex.getMessage(), request.getRequestURI());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex instanceof NotFoundException) status = HttpStatus.NOT_FOUND;
        else if (ex instanceof AlreadyExistsException) status = HttpStatus.CONFLICT;
        else if (ex instanceof ForbiddenException) status = HttpStatus.FORBIDDEN;
        else if (ex instanceof UnauthorizedException) status = HttpStatus.UNAUTHORIZED;
        return buildResponse(status, code, ex.getMessage(), request.getRequestURI());
    }


    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ApiError> handleGrpcStatusRuntimeException(
            StatusRuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("gRPC call failed: status={}, description={}, path={}", ex.getStatus().getCode(), ex.getStatus().getDescription(), request.getRequestURI());
        String description = ex.getStatus().getDescription() != null ? ex.getStatus().getDescription() : ex.getMessage();
        
        String customCode = null;
        if (ex.getTrailers() != null) {
            customCode = ex.getTrailers().get(io.grpc.Metadata.Key.of("error-code", io.grpc.Metadata.ASCII_STRING_MARSHALLER));
        }

        switch (ex.getStatus().getCode()) {
            case INVALID_ARGUMENT:
                return buildResponse(HttpStatus.BAD_REQUEST, customCode != null ? customCode : "BAD_REQUEST", description, request.getRequestURI());
            case NOT_FOUND:
                return buildResponse(HttpStatus.NOT_FOUND, customCode != null ? customCode : "NOT_FOUND", description, request.getRequestURI());
            case ALREADY_EXISTS:
                return buildResponse(HttpStatus.CONFLICT, customCode != null ? customCode : "CONFLICT", description, request.getRequestURI());
            case UNAUTHENTICATED:
            case PERMISSION_DENIED:
                return buildResponse(HttpStatus.FORBIDDEN, customCode != null ? customCode : "FORBIDDEN", description, request.getRequestURI());
            default:
                String message = "gRPC Error: " + ex.getStatus().getCode() + " - " + description;
                return buildResponse(HttpStatus.BAD_GATEWAY, customCode != null ? customCode : "GRPC_ERROR", message, request.getRequestURI());
        }
    }

}

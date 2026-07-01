package com.furnisight.message.exception;

import com.furnisight.message.domain.exceptions.*;
import io.grpc.StatusRuntimeException;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Invalid request parameter: " + ex.getName(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception in MessageService", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getRequestURI()
        );
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String code, String message, String path) {
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message != null ? message : status.getReasonPhrase(),
                path
        );
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

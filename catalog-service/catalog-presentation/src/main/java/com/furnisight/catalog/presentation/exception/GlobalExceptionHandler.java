package com.furnisight.catalog.presentation.exception;

import com.furnisight.catalog.domain.exceptions.BaseException;
import com.furnisight.catalog.domain.exceptions.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFoundException(ProductNotFoundException ex){
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getErrorCode().name(), ex.getMessage());
    }


    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBaseException(BaseException ex){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode().name(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex){
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", "The system is currently under maintenance or experiencing an issue. Please try again later.");
    }

    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String errorCode, String message){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("error_code", errorCode);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }


}



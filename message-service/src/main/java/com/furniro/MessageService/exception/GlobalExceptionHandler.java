package com.furniro.MessageService.exception;

import com.furniro.MessageService.dto.API.AType;
import com.furniro.MessageService.dto.API.ErrorType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<AType> handleAppExceptions(BaseException ex) {
        AType error = ErrorType.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getCode()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<AType> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        AType error = ErrorType.builder()
                .code(400)
                .message("Invalid request parameter: " + ex.getName())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AType> handleUnexpectedException(Exception ex) {
        log.error("Unhandled exception in MessageService", ex);

        AType error = ErrorType.builder()
                .code(500)
                .message("Internal server error")
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

package com.furnisight.gateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ApiError>> handle(
        ResponseStatusException ex
    ) {

        var status = ex.getStatusCode();
        ApiError body = new ApiError(
            LocalDateTime.now(),
            status.value(),
            status.toString(),
            "GATEWAY_ERROR",
            ex.getReason() != null ? ex.getReason() : "Gateway request failed",
            null
        );

        return Mono.just(
            ResponseEntity
                .status(status)
                .body(body)
        );
    }
}

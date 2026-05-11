package com.furnisight.gateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handle(
        ResponseStatusException ex
    ) {

        Map<String, Object> body = Map.of(
            "status", ex.getStatusCode().value(),
            "message", ex.getReason()
        );

        return Mono.just(
            ResponseEntity
                .status(ex.getStatusCode())
                .body(body)
        );
    }
}

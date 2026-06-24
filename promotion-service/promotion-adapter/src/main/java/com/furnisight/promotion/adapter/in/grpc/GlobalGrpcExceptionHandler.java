package com.furnisight.promotion.adapter.in.grpc;

import io.grpc.Status;
import io.grpc.StatusException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusException handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Validation error: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asException();
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusException handleException(Exception e) {
        if (e.getClass().getSimpleName().contains("ValidationException")) {
            log.error("Validation error: {}", e.getMessage());
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asException();
        }
        log.error("Internal server error: ", e);
        return Status.INTERNAL.withDescription(e.getMessage() != null ? e.getMessage() : "Internal server error").withCause(e).asException();
    }
}

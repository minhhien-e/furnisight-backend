package com.furnisight.order.adapter.in.grpc;

import io.grpc.Status;
import io.grpc.StatusException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import com.furnisight.order.domain.exceptions.DomainException;
import com.furnisight.order.domain.exceptions.AlreadyExistsException;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ForbiddenException;
import com.furnisight.order.domain.exceptions.InvalidOperationException;
import com.furnisight.order.domain.exceptions.NotFoundException;
import com.furnisight.order.domain.exceptions.UnauthorizedException;

@Slf4j
@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusException handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Validation error: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asException();
    }

    @GrpcExceptionHandler(DomainException.class)
    public StatusException handleDomainException(DomainException e) {
        log.error("Domain exception: {}", e.getMessage());
        
        io.grpc.Metadata trailers = new io.grpc.Metadata();
        if (e.getErrorCode() != null) {
            trailers.put(io.grpc.Metadata.Key.of("error-code", io.grpc.Metadata.ASCII_STRING_MARSHALLER), e.getErrorCode().name());
        }
        
        Status status = Status.INVALID_ARGUMENT;
        if (e instanceof NotFoundException) status = Status.NOT_FOUND;
        else if (e instanceof AlreadyExistsException) status = Status.ALREADY_EXISTS;
        else if (e instanceof ForbiddenException) status = Status.PERMISSION_DENIED;
        else if (e instanceof UnauthorizedException) status = Status.UNAUTHENTICATED;
        else if (e instanceof InvalidOperationException) {
            if (e.getErrorCode() != null && e.getErrorCode().name().contains("PERMISSION")) {
                status = Status.PERMISSION_DENIED;
            }
        }
        
        return status.withDescription(e.getMessage()).withCause(e).asException(trailers);
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

package com.furnisight.order.application.common.port.in;

public interface UseCase<I, O> {
    O execute(I command);
}

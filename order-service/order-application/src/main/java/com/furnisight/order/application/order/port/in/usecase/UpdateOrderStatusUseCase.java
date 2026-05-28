package com.furnisight.order.application.order.port.in.usecase;

public interface UpdateOrderStatusUseCase {
    void shipOrder(String orderCode);

    void deliverOrder(String orderCode);

    void cancelOrder(String orderCode, java.util.UUID userId);
}

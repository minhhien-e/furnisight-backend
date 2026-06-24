package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;

public interface UpdateOrderStatusUseCase {
    void updateOrderStatus(UpdateOrderStatusCommand command);

    void shipOrder(String orderCode);

    void deliverOrder(String orderCode);

    void confirmReceive(String orderCode, java.util.UUID userId);

    void refundOrder(String orderCode);

    void cancelOrder(String orderCode, java.util.UUID userId);
}

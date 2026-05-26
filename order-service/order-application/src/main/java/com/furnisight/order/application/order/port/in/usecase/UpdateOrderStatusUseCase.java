package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;

public interface UpdateOrderStatusUseCase {
    void updateOrderStatus(UpdateOrderStatusCommand command);
}

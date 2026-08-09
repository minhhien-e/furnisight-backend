package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.port.in.dto.OrderResponse;

public interface CreateOrderUseCase {
    OrderResponse createOrder(CreateOrderCommand command);
}

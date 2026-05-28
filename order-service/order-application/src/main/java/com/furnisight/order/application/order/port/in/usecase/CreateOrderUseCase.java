package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.port.in.dto.OrderCreateProjection;

public interface CreateOrderUseCase {
    OrderCreateProjection createOrder(CreateOrderCommand command);
}

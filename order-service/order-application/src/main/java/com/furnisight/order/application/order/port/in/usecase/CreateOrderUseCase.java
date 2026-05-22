package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import java.util.UUID;

public interface CreateOrderUseCase {
    UUID createOrder(CreateOrderCommand command);
}

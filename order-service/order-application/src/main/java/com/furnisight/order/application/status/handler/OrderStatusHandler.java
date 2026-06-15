package com.furnisight.order.application.status.handler;

import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.domain.enums.OrderStatus;

public interface OrderStatusHandler {
    OrderStatus status();
    void handle(OrderProcessingContext context);
}

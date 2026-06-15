package com.furnisight.order.application.status.handler;

import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderStatusHandlerRegistry {
    private final Map<OrderStatus, OrderStatusHandler> handlers = new EnumMap<>(OrderStatus.class);

    public OrderStatusHandlerRegistry(List<OrderStatusHandler> handlers) {
        handlers.forEach(handler -> {
            if (this.handlers.put(handler.status(), handler) != null) {
                throw new IllegalStateException("Duplicate order status handler: " + handler.status());
            }
        });
        for (OrderStatus status : OrderStatus.values()) {
            if (!this.handlers.containsKey(status)) {
                throw new IllegalStateException("Missing order status handler: " + status);
            }
        }
    }

    public OrderStatusHandler resolve(OrderStatus status) {
        OrderStatusHandler handler = handlers.get(status);
        if (handler == null) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        return handler;
    }
}

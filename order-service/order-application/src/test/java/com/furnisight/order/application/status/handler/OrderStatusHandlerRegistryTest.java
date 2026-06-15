package com.furnisight.order.application.status.handler;

import com.furnisight.order.domain.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderStatusHandlerRegistryTest {
    @Test
    void resolvesEveryRegisteredStatusHandler() {
        List<OrderStatusHandler> handlers = Arrays.stream(OrderStatus.values())
                .map(this::handler)
                .toList();

        OrderStatusHandlerRegistry registry = new OrderStatusHandlerRegistry(handlers);

        handlers.forEach(handler -> assertSame(handler, registry.resolve(handler.status())));
    }

    @Test
    void failsFastWhenAHandlerIsMissing() {
        List<OrderStatusHandler> handlers = Arrays.stream(OrderStatus.values())
                .filter(status -> status != OrderStatus.REFUNDED)
                .map(this::handler)
                .toList();

        assertThrows(IllegalStateException.class, () -> new OrderStatusHandlerRegistry(handlers));
    }

    @Test
    void failsFastWhenAHandlerIsDuplicated() {
        List<OrderStatusHandler> handlers = new java.util.ArrayList<>(
                Arrays.stream(OrderStatus.values()).map(this::handler).toList()
        );
        handlers.add(handler(OrderStatus.PAID));

        assertThrows(IllegalStateException.class, () -> new OrderStatusHandlerRegistry(handlers));
    }

    private OrderStatusHandler handler(OrderStatus status) {
        OrderStatusHandler handler = mock(OrderStatusHandler.class);
        when(handler.status()).thenReturn(status);
        return handler;
    }
}

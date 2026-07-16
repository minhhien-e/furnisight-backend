package com.furnisight.order.application.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.domain.entities.OutboxMessage;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.OutboxMessageRepository;
import com.furnisight.order.application.user.port.out.UserEmailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {
    private final OutboxMessageRepository repository;
    private final ObjectMapper objectMapper;
    private final UserEmailPort userEmailPort;

    public void enqueue(OrderProcessingContext context, OrderStatus previousStatus) {
        String paymentMethod = context.getOrder().getPaymentDetail() != null 
                ? context.getOrder().getPaymentDetail().getPaymentMethod() 
                : null;
                
        OrderStatusChangedPayload payload = new OrderStatusChangedPayload(
                context.getOrder().getUserId(),
                userEmailPort.getEmailByUserId(context.getOrder().getUserId()),
                context.getOrder().getOrderCode(),
                previousStatus,
                context.getOrder().getStatus(),
                paymentMethod,
                LocalDateTime.now());
        repository.save(new OutboxMessage(
                "Order",
                context.getOrder().getOrderCode(),
                "order-status-changed",
                toJson(payload)
        ));

        if (context.getOrder().getStatus() == OrderStatus.DELIVERED) {
            List<OrderItemPayload> items = context.getOrder().getItems().stream()
                    .map(item -> new OrderItemPayload(
                            UUID.fromString(item.getProductSnapshot().getProductId()),
                            item.getQuantity()
                    ))
                    .toList();
            OrderDeliveredPayload deliveredPayload = new OrderDeliveredPayload(
                    context.getOrder().getOrderCode(),
                    items,
                    LocalDateTime.now()
            );
            repository.save(new OutboxMessage(
                    "Order",
                    context.getOrder().getOrderCode(),
                    "order-delivered",
                    toJson(deliveredPayload)
            ));
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize order notification event", exception);
        }
    }

    private record OrderStatusChangedPayload(
            UUID userId,
            String customerEmail,
            String orderCode,
            OrderStatus previousStatus,
            OrderStatus nextStatus,
            String paymentMethod,
            LocalDateTime occurredAt
    ) {}

    private record OrderDeliveredPayload(
            String orderCode,
            List<OrderItemPayload> items,
            LocalDateTime occurredAt
    ) {}

    private record OrderItemPayload(
            UUID productId,
            Integer quantity
    ) {}
}

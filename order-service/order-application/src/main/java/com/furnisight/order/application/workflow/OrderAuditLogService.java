package com.furnisight.order.application.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.domain.entities.OutboxMessage;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderAuditLogService {
    private final OutboxMessageRepository repository;
    private final ObjectMapper objectMapper;

    public void enqueue(OrderProcessingContext context, OrderStatus previousStatus) {
        OrderAuditPayload payload = new OrderAuditPayload(
                context.getActorId(),
                context.getActorType(),
                context.getOrder().getOrderCode(),
                previousStatus,
                context.getOrder().getStatus(),
                context.getOrder().getTrackingCode(),
                context.getNote(),
                LocalDateTime.now());
        repository.save(new OutboxMessage(
                "Order",
                context.getOrder().getOrderCode(),
                "order-audit",
                toJson(payload)
        ));
    }

    private String toJson(OrderAuditPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize order audit event", exception);
        }
    }

    private record OrderAuditPayload(
            UUID actorId,
            String actorType,
            String orderCode,
            OrderStatus previousStatus,
            OrderStatus nextStatus,
            String trackingCode,
            String note,
            LocalDateTime occurredAt
    ) {
    }
}

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
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderAuditLogService {
    private final OutboxMessageRepository repository;
    private final ObjectMapper objectMapper;

    public void enqueue(OrderProcessingContext context, OrderStatus previousStatus) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("actorId", context.getActorId());
        payload.put("actorType", context.getActorType());
        payload.put("orderCode", context.getOrder().getOrderCode());
        payload.put("previousStatus", previousStatus);
        payload.put("nextStatus", context.getOrder().getStatus());
        payload.put("trackingCode", context.getOrder().getTrackingCode());
        payload.put("note", context.getNote());
        payload.put("occurredAt", LocalDateTime.now());
        repository.save(new OutboxMessage(
                "Order",
                context.getOrder().getOrderCode(),
                "order-audit",
                toJson(payload)
        ));
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize order audit event", exception);
        }
    }
}

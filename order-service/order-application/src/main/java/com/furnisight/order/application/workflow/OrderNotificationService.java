package com.furnisight.order.application.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.domain.entities.OutboxMessage;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.OutboxMessageRepository;
import com.furnisight.order.application.user.port.out.UserEmailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {
    private final OutboxMessageRepository repository;
    private final ObjectMapper objectMapper;
    private final UserEmailPort userEmailPort;

    public void enqueue(OrderProcessingContext context, OrderStatus previousStatus) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", context.getOrder().getUserId());
        payload.put("customerEmail", userEmailPort.getEmailByUserId(context.getOrder().getUserId()));
        payload.put("orderCode", context.getOrder().getOrderCode());
        payload.put("previousStatus", previousStatus);
        payload.put("nextStatus", context.getOrder().getStatus());
        payload.put("occurredAt", LocalDateTime.now());
        repository.save(new OutboxMessage(
                "Order",
                context.getOrder().getOrderCode(),
                "order-status-changed",
                toJson(payload)
        ));
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize order notification event", exception);
        }
    }
}

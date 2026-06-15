package com.furnisight.admin.audit.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.admin.audit.application.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAuditEventConsumer {
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @KafkaListener(topics = "order-audit")
    public void handle(String payload) {
        try {
            JsonNode event = parsePayload(payload);
            auditLogService.recordOrderEvent(
                    uuid(event.path("actorId").asText(null)),
                    event.path("orderCode").asText(""),
                    event.path("previousStatus").asText(""),
                    event.path("nextStatus").asText(""),
                    event.path("trackingCode").asText(null),
                    event.path("note").asText(null)
            );
        } catch (Exception exception) {
            log.error("Failed to consume order audit event", exception);
            throw new IllegalStateException("Invalid order audit event", exception);
        }
    }

    private JsonNode parsePayload(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        return node.isTextual() ? objectMapper.readTree(node.asText()) : node;
    }

    private UUID uuid(String value) {
        try {
            return value == null || value.isBlank() || "null".equals(value)
                    ? null
                    : UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}

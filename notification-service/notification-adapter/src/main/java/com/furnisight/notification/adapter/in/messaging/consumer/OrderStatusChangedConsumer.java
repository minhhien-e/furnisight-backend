package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.application.notification.port.in.dto.command.ReceiveNotificationCommand;
import com.furnisight.notification.application.notification.port.in.usecase.ReceiveNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusChangedConsumer {
    private static final Map<String, String> STATUS_LABELS = Map.of(
            "PAID", "Đã thanh toán",
            "SHIPPING", "Đang giao",
            "DELIVERED", "Hoàn tất",
            "CANCELLED", "Đã hủy",
            "PAYMENT_FAILED", "Thanh toán thất bại",
            "REFUND_PENDING", "Chờ hoàn tiền",
            "REFUNDED", "Đã hoàn tiền"
    );

    private final ObjectMapper objectMapper;
    private final ReceiveNotificationUseCase receiveNotificationUseCase;

    @KafkaListener(topics = "order-status-changed")
    public void handle(String payload) {
        try {
            JsonNode event = parsePayload(payload);
            UUID userId = UUID.fromString(event.path("userId").asText());
            String orderCode = event.path("orderCode").asText();
            String nextStatus = event.path("nextStatus").asText();
            receiveNotificationUseCase.execute(ReceiveNotificationCommand.builder()
                    .userId(userId)
                    .title("Cập nhật đơn hàng " + orderCode)
                    .body("Đơn hàng của bạn đã chuyển sang trạng thái "
                            + STATUS_LABELS.getOrDefault(nextStatus, nextStatus) + ".")
                    .image("")
                    .actionUrl("/account?view=orders&order=" + orderCode)
                    .type(NotificationType.ORDER)
                    .build());
        } catch (Exception exception) {
            log.error("Failed to consume order status event", exception);
            throw new IllegalStateException("Invalid order status event", exception);
        }
    }

    private JsonNode parsePayload(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        return node.isTextual() ? objectMapper.readTree(node.asText()) : node;
    }
}

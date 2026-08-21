package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.OrderStatusChangedEvent;
import com.furnisight.notification.adapter.in.messaging.dto.TemplateEventData;
import com.furnisight.notification.application.notification.port.in.dto.command.RenderNotificationCommand;
import com.furnisight.notification.application.notification.port.in.dto.command.ReceiveNotificationCommand;
import com.furnisight.notification.application.notification.port.in.usecase.ReceiveNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrderStatusChangedConsumer extends AbstractNotificationConsumer<OrderStatusChangedEvent> {
    private static final String TOPIC = "order-status-changed";
    private static final String TEMPLATE_CODE = "order-status-changed";



    private final ObjectMapper objectMapper;
    private final ReceiveNotificationUseCase receiveNotificationUseCase;
    private final RenderNotificationUseCase renderNotificationUseCase;

    public OrderStatusChangedConsumer(
            SendNotificationUseCase sendNotificationUseCase,
            RenderNotificationUseCase renderNotificationUseCase,
            ObjectMapper objectMapper,
            ReceiveNotificationUseCase receiveNotificationUseCase) {
        super(sendNotificationUseCase, renderNotificationUseCase, objectMapper, OrderStatusChangedEvent.class, TEMPLATE_CODE);
        this.objectMapper = objectMapper;
        this.receiveNotificationUseCase = receiveNotificationUseCase;
        this.renderNotificationUseCase = renderNotificationUseCase;
    }

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) {
        try {
            JsonNode node = parsePayload(payload);
            OrderStatusChangedEvent event = objectMapper.treeToValue(node, OrderStatusChangedEvent.class);
            String statusDescription = mapNextStatusToDescription(event.nextStatus());
            var eventDataNode = objectMapper.valueToTree(java.util.Map.of(
                    "orderCode", event.orderCode() != null ? event.orderCode() : "",
                    "nextStatus", statusDescription,
                    "paymentMethod", event.paymentMethod() != null ? event.paymentMethod() : ""
            ));

            var renderResult = renderNotificationUseCase.execute(RenderNotificationCommand.builder()
                    .templateCode("order-status-changed-push")
                    .data(new TemplateEventData(eventDataNode))
                    .build());

            // 1. Send Push Notification (In-App)
            receiveNotificationUseCase.execute(ReceiveNotificationCommand.builder()
                    .userId(event.userId())
                    .title(renderResult.getTitle())
                    .body(renderResult.getBody())
                    .image("")
                    .actionUrl("/account?view=orders&order=" + event.orderCode())
                    .type(NotificationType.ORDER)
                    .build());

            // 2. Send Email Notification
            processEvent(objectMapper.writeValueAsString(event));
            
        } catch (Exception exception) {
            log.error("Failed to consume order status event", exception);
            throw new IllegalStateException("Invalid order status event", exception);
        }
    }

    private String mapNextStatusToDescription(String status) {
        if (status == null) return "vừa chuyển sang trạng thái mới.";
        return switch (status.toUpperCase()) {
            case "PAID" -> "đã được thanh toán thành công.";
            case "SHIPPING" -> "đang trên đường giao đến bạn.";
            case "DELIVERED" -> "giao hàng thành công. Cảm ơn bạn!";
            case "CANCELLED" -> "đã bị hủy.";
            case "PROCESSING" -> "đang được xử lý.";
            case "PENDING" -> "đang chờ thanh toán.";
            case "CONFIRMED" -> "đã được xác nhận.";
            case "REFUNDED" -> "đã được hoàn tiền.";
            default -> "vừa chuyển sang trạng thái " + status + ".";
        };
    }

    private JsonNode parsePayload(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        return node.isTextual() ? objectMapper.readTree(node.asText()) : node;
    }

    @Override
    protected UUID getAccountId(OrderStatusChangedEvent event) {
        return event.userId();
    }

    @Override
    protected String getDestination(OrderStatusChangedEvent event) {
        return event.customerEmail();
    }

    @Override
    protected NotificationChannel getChannel(OrderStatusChangedEvent event) {
        return NotificationChannel.EMAIL;
    }
}

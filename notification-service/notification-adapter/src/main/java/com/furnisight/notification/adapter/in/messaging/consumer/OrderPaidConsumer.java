package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.OrderPaidEvent;
import com.furnisight.notification.application.notification.port.in.dto.command.ReceiveNotificationCommand;
import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.in.usecase.ReceiveNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class OrderPaidConsumer {
    private static final String TOPIC = "order-paid";

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ReceiveNotificationUseCase receiveNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        OrderPaidEvent event = objectMapper.readValue(normalizePayload(payload), OrderPaidEvent.class);
        String title = "Thanh toán đơn hàng " + event.orderCode() + " thành công";
        String body = "Đơn hàng " + event.orderCode() + " đã được thanh toán thành công"
                + amountText(event.paidAmount())
                + methodText(event.paymentMethod())
                + ".";
        String actionUrl = "/account?view=orders&order=" + event.orderCode();

        sendNotificationUseCase.execute(SendNotificationCommand.builder()
                .userId(event.userId())
                .destination(event.customerEmail())
                .title(title)
                .body(body)
                .image("")
                .actionUrl(actionUrl)
                .type(NotificationType.ORDER)
                .channel(NotificationChannel.EMAIL)
                .build());

        receiveNotificationUseCase.execute(ReceiveNotificationCommand.builder()
                .userId(event.userId())
                .title(title)
                .body(body)
                .image("")
                .actionUrl(actionUrl)
                .type(NotificationType.ORDER)
                .build());
    }

    private String amountText(Double paidAmount) {
        return paidAmount == null ? "" : " với số tiền " + formatAmount(paidAmount);
    }

    private String methodText(String paymentMethod) {
        return paymentMethod == null || paymentMethod.isBlank() ? "" : " qua " + paymentMethod;
    }

    private String formatAmount(Double amount) {
        return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString();
    }

    private String normalizePayload(String payload) throws JsonProcessingException {
        var node = objectMapper.readTree(payload);
        return node.isTextual() ? node.asText() : payload;
    }
}

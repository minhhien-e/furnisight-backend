package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.OrderPlacedEvent;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderPlacedConsumer extends AbstractNotificationConsumer<OrderPlacedEvent> {
    private final static String TOPIC = "order-placed";
    private final static String TEMPLATE_CODE = "order-placed";

    public OrderPlacedConsumer(
            SendNotificationUseCase sendNotificationUseCase,
            RenderNotificationUseCase renderNotificationUseCase,
            ObjectMapper objectMapper) {
        super(sendNotificationUseCase, renderNotificationUseCase, objectMapper, OrderPlacedEvent.class, TEMPLATE_CODE);
    }

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        processEvent(payload);
    }

    @Override
    protected UUID getAccountId(OrderPlacedEvent event) {
        return event.userId();
    }

    @Override
    protected String getDestination(OrderPlacedEvent event) {
        return event.customerEmail();
    }

    @Override
    protected NotificationChannel getChannel(OrderPlacedEvent event) {
        return NotificationChannel.EMAIL;
    }
}

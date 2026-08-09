package com.furnisight.promotion.adapter.out.integration;

import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.domain.enums.MarketingChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class KafkaMarketingNotificationGateway implements MarketingNotificationGateway {
    static final String TOPIC = "marketing-notification-requested";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public DispatchResult send(String title, String body, String actionUrl,
                               List<MarketingChannel> channels, List<Recipient> recipients, java.util.Map<String, Object> metadata) {
        int accepted = 0;
        int sent = 0;
        List<String> failures = new ArrayList<>();
        for (Recipient recipient : recipients) {
            for (MarketingChannel channel : channels) {
                if (channel == MarketingChannel.EMAIL && (recipient.email() == null || recipient.email().isBlank())) {
                    continue;
                }
                accepted++;
                try {
                    MarketingDeliveryEvent event = new MarketingDeliveryEvent(
                            UUID.randomUUID(), LocalDateTime.now(), recipient.userId(), recipient.email(),
                            title, body, actionUrl, channel == MarketingChannel.EMAIL ? "EMAIL" : "IN_APP", metadata);
                    kafkaTemplate.send(TOPIC, recipient.userId().toString(), event).get(10, TimeUnit.SECONDS);
                    sent++;
                } catch (Exception ex) {
                    failures.add(recipient.userId() + "/" + channel.name() + ": " + ex.getMessage());
                }
            }
        }
        return new DispatchResult(accepted, sent, failures.size(), List.copyOf(failures));
    }

    public record MarketingDeliveryEvent(
            UUID eventId,
            LocalDateTime occurredOn,
            UUID userId,
            String destination,
            String title,
            String body,
            String actionUrl,
            String channel,
            java.util.Map<String, Object> metadata) {
    }
}

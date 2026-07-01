package com.furnisight.promotion.application.port;

import com.furnisight.promotion.application.port.MarketingNotificationGateway;

import com.furnisight.promotion.domain.enums.MarketingChannel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MarketingNotificationGateway {
    DispatchResult send(String title, String body, String actionUrl, List<MarketingChannel> channels, List<Recipient> recipients, Map<String, Object> metadata);

    record Recipient(UUID userId, String email, String name) {}
    record DispatchResult(int acceptedCount, int sentCount, int failedCount, List<String> failures) {}
}

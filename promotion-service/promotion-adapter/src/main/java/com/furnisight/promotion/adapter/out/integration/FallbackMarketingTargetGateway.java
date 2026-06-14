package com.furnisight.promotion.adapter.out.integration;

import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.application.port.MarketingTargetGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class FallbackMarketingTargetGateway implements MarketingTargetGateway {
    @Override
    public List<MarketingNotificationGateway.Recipient> getUsersByIds(List<UUID> userIds) {
        return userIds.stream()
                .map(id -> new MarketingNotificationGateway.Recipient(id, "", "User " + id.toString().substring(0, 8)))
                .toList();
    }

    @Override
    public List<MarketingNotificationGateway.Recipient> getAllActiveUsers() {
        log.warn("Marketing target fallback returns empty ALL segment until user gRPC is connected");
        return List.of();
    }

    @Override
    public List<MarketingNotificationGateway.Recipient> getSegmentUsers(String segmentKey) {
        log.warn("Marketing target fallback returns empty segment={} until segment gRPC is connected", segmentKey);
        return List.of();
    }
}

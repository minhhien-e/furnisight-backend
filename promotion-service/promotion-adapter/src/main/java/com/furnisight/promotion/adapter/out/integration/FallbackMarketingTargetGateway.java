package com.furnisight.promotion.adapter.out.integration;

import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.application.port.MarketingTargetGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class FallbackMarketingTargetGateway implements MarketingTargetGateway {
    @Override
    public List<MarketingNotificationGateway.Recipient> getUsersByIds(List<UUID> userIds) {
        throw new IllegalStateException("Marketing target integration is not configured");
    }

    @Override
    public List<MarketingNotificationGateway.Recipient> getAllActiveUsers() {
        throw new IllegalStateException("Marketing target integration is not configured");
    }

    @Override
    public List<MarketingNotificationGateway.Recipient> getSegmentUsers(String segmentKey) {
        throw new IllegalStateException("Marketing target integration is not configured");
    }
}

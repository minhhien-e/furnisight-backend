package com.furnisight.promotion.adapter.out.integration;

import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.domain.enums.MarketingChannel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FallbackMarketingNotificationGateway implements MarketingNotificationGateway {
    @Override
    public DispatchResult send(String title, String body, String actionUrl, List<MarketingChannel> channels, List<Recipient> recipients) {
        throw new IllegalStateException("Marketing notification integration is not configured");
    }
}

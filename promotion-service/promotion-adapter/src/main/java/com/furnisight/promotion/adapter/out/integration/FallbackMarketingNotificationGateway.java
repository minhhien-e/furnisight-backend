package com.furnisight.promotion.adapter.out.integration;

import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.domain.enums.MarketingChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FallbackMarketingNotificationGateway implements MarketingNotificationGateway {
    @Override
    public DispatchResult send(String title, String body, String actionUrl, List<MarketingChannel> channels, List<Recipient> recipients) {
        log.info("Marketing notification fallback accepted recipients={}, channels={}, title={}", recipients.size(), channels, title);
        return new DispatchResult(recipients.size(), recipients.size(), 0, List.of());
    }
}

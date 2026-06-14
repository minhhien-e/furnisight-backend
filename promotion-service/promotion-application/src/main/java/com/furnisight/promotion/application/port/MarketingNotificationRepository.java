package com.furnisight.promotion.application.port;

import com.furnisight.promotion.domain.entities.MarketingNotification;
import com.furnisight.promotion.domain.enums.CampaignStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketingNotificationRepository {
    List<MarketingNotification> findAll();
    Optional<MarketingNotification> findById(UUID id);
    MarketingNotification save(MarketingNotification notification);
    void deleteById(UUID id);
    List<MarketingNotification> findDueScheduled(CampaignStatus status, LocalDateTime now);
}

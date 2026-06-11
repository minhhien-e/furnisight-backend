package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.MarketingNotification;
import com.furnisight.promotion.domain.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MarketingNotificationJpaRepository extends JpaRepository<MarketingNotification, UUID> {
    List<MarketingNotification> findByStatusAndScheduledAtLessThanEqual(CampaignStatus status, LocalDateTime scheduledAt);
}

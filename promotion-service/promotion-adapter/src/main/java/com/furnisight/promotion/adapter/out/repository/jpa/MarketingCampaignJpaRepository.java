package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.MarketingCampaign;
import com.furnisight.promotion.domain.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MarketingCampaignJpaRepository extends JpaRepository<MarketingCampaign, UUID> {
    List<MarketingCampaign> findByStatusAndScheduledAtLessThanEqual(CampaignStatus status, LocalDateTime scheduledAt);
}

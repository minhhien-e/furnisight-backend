package com.furnisight.promotion.application.port;

import com.furnisight.promotion.domain.entities.MarketingCampaign;
import com.furnisight.promotion.domain.enums.CampaignStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketingCampaignRepository {
    List<MarketingCampaign> findAll();
    Optional<MarketingCampaign> findById(UUID id);
    MarketingCampaign save(MarketingCampaign campaign);
    void deleteById(UUID id);
    List<MarketingCampaign> findDueScheduled(CampaignStatus status, LocalDateTime now);
}

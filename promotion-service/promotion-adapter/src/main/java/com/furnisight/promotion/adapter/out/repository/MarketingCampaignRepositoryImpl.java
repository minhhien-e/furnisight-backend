package com.furnisight.promotion.adapter.out.repository;

import com.furnisight.promotion.adapter.out.repository.jpa.MarketingCampaignJpaRepository;
import com.furnisight.promotion.application.port.MarketingCampaignRepository;
import com.furnisight.promotion.domain.entities.MarketingCampaign;
import com.furnisight.promotion.domain.enums.CampaignStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MarketingCampaignRepositoryImpl implements MarketingCampaignRepository {
    private final MarketingCampaignJpaRepository jpaRepository;
    public List<MarketingCampaign> findAll() { return jpaRepository.findAll(); }
    public Optional<MarketingCampaign> findById(UUID id) { return jpaRepository.findById(id); }
    public MarketingCampaign save(MarketingCampaign campaign) { return jpaRepository.save(campaign); }
    public void deleteById(UUID id) { jpaRepository.deleteById(id); }
    public List<MarketingCampaign> findDueScheduled(CampaignStatus status, LocalDateTime now) {
        return jpaRepository.findByStatusAndScheduledAtLessThanEqual(status, now);
    }
}

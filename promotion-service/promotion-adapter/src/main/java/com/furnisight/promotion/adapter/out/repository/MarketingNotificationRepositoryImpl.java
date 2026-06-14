package com.furnisight.promotion.adapter.out.repository;

import com.furnisight.promotion.adapter.out.repository.jpa.MarketingNotificationJpaRepository;
import com.furnisight.promotion.application.port.MarketingNotificationRepository;
import com.furnisight.promotion.domain.entities.MarketingNotification;
import com.furnisight.promotion.domain.enums.CampaignStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MarketingNotificationRepositoryImpl implements MarketingNotificationRepository {
    private final MarketingNotificationJpaRepository jpaRepository;
    public List<MarketingNotification> findAll() { return jpaRepository.findAll(); }
    public Optional<MarketingNotification> findById(UUID id) { return jpaRepository.findById(id); }
    public MarketingNotification save(MarketingNotification notification) { return jpaRepository.save(notification); }
    public void deleteById(UUID id) { jpaRepository.deleteById(id); }
    public List<MarketingNotification> findDueScheduled(CampaignStatus status, LocalDateTime now) {
        return jpaRepository.findByStatusAndScheduledAtLessThanEqual(status, now);
    }
}

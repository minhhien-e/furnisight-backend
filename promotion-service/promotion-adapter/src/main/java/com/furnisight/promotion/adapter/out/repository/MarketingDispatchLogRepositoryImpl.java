package com.furnisight.promotion.adapter.out.repository;

import com.furnisight.promotion.adapter.out.repository.jpa.MarketingDispatchLogJpaRepository;
import com.furnisight.promotion.application.port.MarketingDispatchLogRepository;
import com.furnisight.promotion.domain.entities.MarketingDispatchLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MarketingDispatchLogRepositoryImpl implements MarketingDispatchLogRepository {
    private final MarketingDispatchLogJpaRepository jpaRepository;
    public MarketingDispatchLog save(MarketingDispatchLog log) { return jpaRepository.save(log); }
}

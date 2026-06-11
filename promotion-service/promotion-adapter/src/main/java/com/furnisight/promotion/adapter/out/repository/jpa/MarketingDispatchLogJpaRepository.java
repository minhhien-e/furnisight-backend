package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.MarketingDispatchLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MarketingDispatchLogJpaRepository extends JpaRepository<MarketingDispatchLog, UUID> {
}

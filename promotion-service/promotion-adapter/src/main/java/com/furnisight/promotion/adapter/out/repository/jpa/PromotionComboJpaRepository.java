package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.PromotionCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

public interface PromotionComboJpaRepository extends JpaRepository<PromotionCombo, UUID> {
    List<PromotionCombo> findByActiveTrue();

    @Query("""
            SELECT c FROM PromotionCombo c
            WHERE c.active = true
              AND (c.startDate IS NULL OR c.startDate <= :now)
              AND (c.endDate IS NULL OR c.endDate >= :now)
            """)
    Page<PromotionCombo> findActivePage(@Param("now") LocalDateTime now, Pageable pageable);
}

package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.PromotionCombo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionComboJpaRepository extends JpaRepository<PromotionCombo, UUID> {
    List<PromotionCombo> findByActiveTrue();
}

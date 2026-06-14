package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.PromotionComboItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionComboItemJpaRepository extends JpaRepository<PromotionComboItem, UUID> {
    List<PromotionComboItem> findByComboId(UUID comboId);
    void deleteByComboId(UUID comboId);
}

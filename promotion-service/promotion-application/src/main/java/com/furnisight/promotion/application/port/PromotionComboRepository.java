package com.furnisight.promotion.application.port;

import com.furnisight.promotion.domain.entities.PromotionCombo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionComboRepository {
    List<PromotionCombo> findAll();
    List<PromotionCombo> findActive();
    Optional<PromotionCombo> findById(UUID id);
    PromotionCombo save(PromotionCombo combo);
    void deleteById(UUID id);
}

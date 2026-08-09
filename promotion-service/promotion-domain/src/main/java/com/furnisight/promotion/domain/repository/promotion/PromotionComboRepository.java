package com.furnisight.promotion.domain.repository.promotion;

import com.furnisight.promotion.domain.entities.PromotionCombo;
import com.furnisight.promotion.domain.common.PageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionComboRepository {
    List<PromotionCombo> findAll();
    List<PromotionCombo> findActive();
    PageResponse<PromotionCombo> findActivePage(LocalDateTime now, int page, int size, String sort);
    Optional<PromotionCombo> findById(UUID id);
    PromotionCombo save(PromotionCombo combo);
    void deleteById(UUID id);
}

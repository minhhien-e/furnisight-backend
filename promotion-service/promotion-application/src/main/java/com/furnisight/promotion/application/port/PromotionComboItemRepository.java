package com.furnisight.promotion.application.port;

import com.furnisight.promotion.domain.entities.PromotionComboItem;

import java.util.List;
import java.util.Collection;
import java.util.UUID;
import java.util.UUID;

public interface PromotionComboItemRepository {
    List<PromotionComboItem> findByComboId(UUID comboId);
    List<PromotionComboItem> findByComboIds(Collection<UUID> comboIds);
    PromotionComboItem save(PromotionComboItem item);
    void deleteByComboId(UUID comboId);
}

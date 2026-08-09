package com.furnisight.promotion.adapter.out.repository;

import com.furnisight.promotion.adapter.out.repository.jpa.PromotionComboItemJpaRepository;
import com.furnisight.promotion.domain.repository.promotion.PromotionComboItemRepository;
import com.furnisight.promotion.domain.entities.PromotionComboItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PromotionComboItemRepositoryImpl implements PromotionComboItemRepository {
    private final PromotionComboItemJpaRepository jpaRepository;
    public List<PromotionComboItem> findByComboId(UUID comboId) { return jpaRepository.findByComboId(comboId); }
    public List<PromotionComboItem> findByComboIds(Collection<UUID> comboIds) { return jpaRepository.findByComboIdIn(comboIds); }
    public PromotionComboItem save(PromotionComboItem item) { return jpaRepository.save(item); }
    @Transactional
    public void deleteByComboId(UUID comboId) { jpaRepository.deleteByComboId(comboId); }
}

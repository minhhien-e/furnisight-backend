package com.furnisight.promotion.adapter.out.repository;

import com.furnisight.promotion.adapter.out.repository.jpa.PromotionComboJpaRepository;
import com.furnisight.promotion.application.port.PromotionComboRepository;
import com.furnisight.promotion.domain.entities.PromotionCombo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PromotionComboRepositoryImpl implements PromotionComboRepository {
    private final PromotionComboJpaRepository jpaRepository;
    public List<PromotionCombo> findAll() { return jpaRepository.findAll(); }
    public List<PromotionCombo> findActive() { return jpaRepository.findByActiveTrue(); }
    public Optional<PromotionCombo> findById(UUID id) { return jpaRepository.findById(id); }
    public PromotionCombo save(PromotionCombo combo) { return jpaRepository.save(combo); }
    public void deleteById(UUID id) { jpaRepository.deleteById(id); }
}

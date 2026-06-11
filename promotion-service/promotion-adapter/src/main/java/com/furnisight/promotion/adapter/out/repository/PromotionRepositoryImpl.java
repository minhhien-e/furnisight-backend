package com.furnisight.promotion.adapter.out.repository;

import com.furnisight.promotion.adapter.out.repository.jpa.PromotionJpaRepository;
import com.furnisight.promotion.application.port.PromotionRepository;
import com.furnisight.promotion.domain.entities.Promotion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PromotionRepositoryImpl implements PromotionRepository {
    private final PromotionJpaRepository jpaRepository;

    @Override
    public Optional<Promotion> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Promotion> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public List<Promotion> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Promotion> findAllActive() {
        return jpaRepository.findAllActive();
    }

    @Override
    public Promotion save(Promotion promotion) {
        return jpaRepository.save(promotion);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

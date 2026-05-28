package com.furnisight.order.adapter.out.repository.promotion.jpa.impl;

import com.furnisight.order.adapter.out.repository.promotion.jpa.PromotionJpaRepository;
import com.furnisight.order.application.promotion.port.out.repository.PromotionRepository;
import com.furnisight.order.domain.entities.promotion.Promotion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PromotionRepositoryImpl implements PromotionRepository {

    private final PromotionJpaRepository jpaRepository;

    @Override
    public Optional<Promotion> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public List<Promotion> findAllActive() {
        return jpaRepository.findAllActive();
    }

    @Override
    public Promotion save(Promotion promotion) {
        return jpaRepository.save(promotion);
    }
}

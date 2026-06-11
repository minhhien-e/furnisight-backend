package com.furnisight.promotion.application.port;

import com.furnisight.promotion.domain.entities.Promotion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository {
    Optional<Promotion> findById(UUID id);
    Optional<Promotion> findByCode(String code);
    List<Promotion> findAll();
    List<Promotion> findAllActive();
    Promotion save(Promotion promotion);
    void deleteById(UUID id);
}

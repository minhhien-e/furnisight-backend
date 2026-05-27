package com.furnisight.order.application.promotion.port.out.repository;

import com.furnisight.order.domain.entities.promotion.Promotion;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository {
    Optional<Promotion> findByCode(String code);
    List<Promotion> findAllActive();
    Promotion save(Promotion promotion);
}

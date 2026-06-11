package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionJpaRepository extends JpaRepository<Promotion, UUID> {
    Optional<Promotion> findByCode(String code);

    @Query("SELECT p FROM Promotion p WHERE p.active = true")
    List<Promotion> findAllActive();
}

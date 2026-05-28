package com.furnisight.order.adapter.out.repository.promotion.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.furnisight.order.domain.entities.promotion.Promotion;

@Repository
public interface PromotionJpaRepository extends JpaRepository<Promotion, UUID> {
    Optional<Promotion> findByCode(String code);

    @Query("SELECT p FROM Promotion p WHERE p.active = true")
    List<Promotion> findAllActive();
}

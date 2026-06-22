package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import com.furnisight.promotion.domain.enums.DiscountType;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionJpaRepository extends JpaRepository<Promotion, UUID> {
    Optional<Promotion> findByCode(String code);

    @Query("SELECT p FROM Promotion p WHERE p.active = true")
    List<Promotion> findAllActive();

    @Query("""
            SELECT p FROM Promotion p
            WHERE p.voucherType = com.furnisight.promotion.domain.enums.VoucherType.PUBLIC
              AND p.active = true
              AND (p.startDate IS NULL OR p.startDate <= :now)
              AND (p.endDate IS NULL OR p.endDate >= :now)
              AND (:expiresBefore IS NULL OR (p.endDate IS NOT NULL AND p.endDate <= :expiresBefore))
              AND (:shippingOnly = false OR p.discountType = :shippingType)
            """)
    Page<Promotion> findPublicActivePage(@Param("now") LocalDateTime now,
                                         @Param("expiresBefore") LocalDateTime expiresBefore,
                                         @Param("shippingOnly") boolean shippingOnly,
                                         @Param("shippingType") DiscountType shippingType,
                                         Pageable pageable);
}

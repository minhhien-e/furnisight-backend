package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserVoucherJpaRepository extends JpaRepository<UserVoucher, UUID> {
    List<UserVoucher> findByUserId(UUID userId);
    @Query("""
            SELECT uv FROM UserVoucher uv JOIN FETCH uv.promotion p
            WHERE uv.userId = :userId AND uv.used = false AND p.active = true
              AND (p.startDate IS NULL OR p.startDate <= :now)
              AND (p.endDate IS NULL OR p.endDate >= :now)
            ORDER BY p.endDate ASC NULLS LAST, p.code ASC
            """)
    List<UserVoucher> findUsableByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    Optional<UserVoucher> findByUserIdAndPromotionId(UUID userId, UUID promotionId);
    long countByPromotionId(UUID promotionId);
}

package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserVoucherJpaRepository extends JpaRepository<UserVoucher, UUID> {
    List<UserVoucher> findByUserId(UUID userId);
    Optional<UserVoucher> findByUserIdAndPromotionId(UUID userId, UUID promotionId);
    long countByPromotionId(UUID promotionId);
}

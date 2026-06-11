package com.furnisight.promotion.adapter.out.repository.jpa;

import com.furnisight.promotion.domain.entities.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserVoucherJpaRepository extends JpaRepository<UserVoucher, UUID> {
    List<UserVoucher> findByUserId(UUID userId);
    long countByPromotionId(UUID promotionId);
}

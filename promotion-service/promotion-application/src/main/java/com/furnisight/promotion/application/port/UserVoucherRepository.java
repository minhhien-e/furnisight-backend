package com.furnisight.promotion.application.port;

import com.furnisight.promotion.domain.entities.UserVoucher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

public interface UserVoucherRepository {
    List<UserVoucher> findByUserId(UUID userId);
    List<UserVoucher> findUsableByUserId(UUID userId, LocalDateTime now);
    Optional<UserVoucher> findByUserIdAndPromotionId(UUID userId, UUID promotionId);
    long countByPromotionId(UUID promotionId);
    long countAll();
    UserVoucher save(UserVoucher userVoucher);
}

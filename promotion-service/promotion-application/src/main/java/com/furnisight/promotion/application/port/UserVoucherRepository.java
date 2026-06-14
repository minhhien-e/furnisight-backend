package com.furnisight.promotion.application.port;

import com.furnisight.promotion.domain.entities.UserVoucher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserVoucherRepository {
    List<UserVoucher> findByUserId(UUID userId);
    Optional<UserVoucher> findByUserIdAndPromotionId(UUID userId, UUID promotionId);
    long countByPromotionId(UUID promotionId);
    long countAll();
    UserVoucher save(UserVoucher userVoucher);
}

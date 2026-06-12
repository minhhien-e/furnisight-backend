package com.furnisight.promotion.adapter.out.repository;

import com.furnisight.promotion.adapter.out.repository.jpa.UserVoucherJpaRepository;
import com.furnisight.promotion.application.port.UserVoucherRepository;
import com.furnisight.promotion.domain.entities.UserVoucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserVoucherRepositoryImpl implements UserVoucherRepository {
    private final UserVoucherJpaRepository jpaRepository;

    @Override
    public List<UserVoucher> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<UserVoucher> findByUserIdAndPromotionId(UUID userId, UUID promotionId) {
        return jpaRepository.findByUserIdAndPromotionId(userId, promotionId);
    }

    @Override
    public long countByPromotionId(UUID promotionId) {
        return jpaRepository.countByPromotionId(promotionId);
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Override
    public UserVoucher save(UserVoucher userVoucher) {
        return jpaRepository.save(userVoucher);
    }
}

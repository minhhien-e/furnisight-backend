package com.furnisight.order.adapter.out.repository.promotion.jpa.impl;

import com.furnisight.order.adapter.out.repository.promotion.jpa.UserVoucherJpaRepository;
import com.furnisight.order.application.promotion.port.out.repository.UserVoucherRepository;
import com.furnisight.order.domain.entities.promotion.UserVoucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserVoucherRepositoryImpl implements UserVoucherRepository {

    private final UserVoucherJpaRepository jpaRepository;

    @Override
    public List<UserVoucher> findByUserId(java.util.UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public UserVoucher save(UserVoucher userVoucher) {
        return jpaRepository.save(userVoucher);
    }
}

package com.furnisight.order.adapter.out.repository.promotion.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import com.furnisight.order.domain.entities.promotion.UserVoucher;

@Repository
public interface UserVoucherJpaRepository extends JpaRepository<UserVoucher, UUID> {
    List<UserVoucher> findByUserId(UUID userId);
}

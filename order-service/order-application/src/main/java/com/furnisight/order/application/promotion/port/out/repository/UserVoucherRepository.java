package com.furnisight.order.application.promotion.port.out.repository;

import com.furnisight.order.domain.entities.promotion.UserVoucher;
import java.util.List;
import java.util.UUID;

public interface UserVoucherRepository {
    List<UserVoucher> findByUserId(UUID userId);
    UserVoucher save(UserVoucher userVoucher);
}

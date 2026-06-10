package com.furnisight.admin.voucher.web.dto.request;

public record UpsertVoucherRequest(
        String code,
        String name,
        String description,
        String icon,
        String discountType,
        double discountValue,
        double maxDiscount,
        double minOrder,
        String startDate,
        String endDate,
        boolean active
) {
}

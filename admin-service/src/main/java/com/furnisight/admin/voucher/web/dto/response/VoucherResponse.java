package com.furnisight.admin.voucher.web.dto.response;

public record VoucherResponse(
        String id,
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
        boolean active,
        String statusLabel
) {
}

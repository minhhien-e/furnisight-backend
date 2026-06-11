package com.furnisight.admin.voucher.web.dto.response;

public record VoucherResponse(
        String id,
        String code,
        String name,
        String description,
        String icon,
        String voucherType,
        String discountType,
        Double discountValue,
        Double maxDiscount,
        Double minOrder,
        String startDate,
        String endDate,
        boolean active,
        String statusLabel,
        long issuedCount
) {
}

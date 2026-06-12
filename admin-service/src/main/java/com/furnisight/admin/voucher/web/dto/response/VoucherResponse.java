package com.furnisight.admin.voucher.web.dto.response;

import java.util.List;

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
        List<String> placements,
        String statusLabel,
        long issuedCount
) {
}

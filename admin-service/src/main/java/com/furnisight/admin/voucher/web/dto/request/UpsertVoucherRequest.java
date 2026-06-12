package com.furnisight.admin.voucher.web.dto.request;

import java.util.List;

public record UpsertVoucherRequest(
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
        Boolean active,
        List<String> placements
) {
}

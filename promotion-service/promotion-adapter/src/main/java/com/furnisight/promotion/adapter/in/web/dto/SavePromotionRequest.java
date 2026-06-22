package com.furnisight.promotion.adapter.in.web.dto;

import java.time.LocalDateTime;

public record SavePromotionRequest(
        String code,
        String name,
        String description,
        String icon,
        String voucherType,
        String discountType,
        Double discountValue,
        Double maxDiscount,
        Double minOrder,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active
) {
}

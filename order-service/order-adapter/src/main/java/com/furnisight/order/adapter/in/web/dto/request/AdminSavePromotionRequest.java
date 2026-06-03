package com.furnisight.order.adapter.in.web.dto.request;

import java.time.LocalDateTime;

public record AdminSavePromotionRequest(
        String code,
        String name,
        String description,
        String icon,
        String discountType,
        Double discountValue,
        Double maxDiscount,
        Double minOrder,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active
) {
}

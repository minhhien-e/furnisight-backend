package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendVouchersResponse {
    private PromotionDto shopVoucher;
    private double shopDiscount;
    private PromotionDto shippingVoucher;
    private double shippingDiscount;
}

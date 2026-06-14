package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidateOrderVouchersResponse {
    private boolean valid;
    private String message;
    private Double discountAmount;
    private Double shippingDiscount;
    private PromotionDto shopVoucher;
    private PromotionDto shippingVoucher;
}

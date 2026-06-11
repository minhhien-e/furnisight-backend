package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidateVoucherResponse {
    private boolean valid;
    private String message;
    private PromotionDto voucher;
    private Double discount;
}

package com.furnisight.promotion.application.dto;

import lombok.Data;

@Data
public class RecommendVouchersCommand {
    private Double subtotal;
    private Double shippingFee;
    private String preferredVoucherCode;
}

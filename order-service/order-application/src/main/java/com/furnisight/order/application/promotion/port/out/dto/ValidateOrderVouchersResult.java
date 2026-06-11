package com.furnisight.order.application.promotion.port.out.dto;

import lombok.Data;

@Data
public class ValidateOrderVouchersResult {
    private boolean valid;
    private String message;
    private Double discountAmount;
    private Double shippingDiscount;
}

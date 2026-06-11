package com.furnisight.order.application.promotion.port.out.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ValidateOrderVouchersRequest {
    private UUID userId;
    private String shopVoucherCode;
    private String shippingVoucherCode;
    private Double subtotal;
    private Double shippingFee;
}

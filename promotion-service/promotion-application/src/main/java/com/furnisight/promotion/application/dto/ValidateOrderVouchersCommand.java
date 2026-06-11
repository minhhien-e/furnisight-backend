package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateOrderVouchersCommand {
    private UUID userId;
    private String shopVoucherCode;
    private String shippingVoucherCode;
    private Double subtotal;
    private Double shippingFee;
}

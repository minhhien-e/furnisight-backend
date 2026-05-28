package com.furnisight.order.application.promotion.port.in.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ValidateVoucherCommand {
    private UUID userId;
    private String code;
    private String type; // 'shop' or 'ship'
    private Double subtotal;
}

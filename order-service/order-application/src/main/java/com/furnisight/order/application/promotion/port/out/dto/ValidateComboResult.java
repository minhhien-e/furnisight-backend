package com.furnisight.order.application.promotion.port.out.dto;

import lombok.Data;

@Data
public class ValidateComboResult {
    private boolean valid;
    private String comboId;
    private String comboName;
    private Double originalAmount;
    private Double finalAmount;
    private Double comboDiscount;
    private String message;
}

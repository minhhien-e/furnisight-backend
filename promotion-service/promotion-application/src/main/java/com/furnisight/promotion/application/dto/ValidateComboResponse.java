package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidateComboResponse {
    private boolean valid;
    private String comboId;
    private String comboName;
    private double originalAmount;
    private double finalAmount;
    private double comboDiscount;
    private String message;
}

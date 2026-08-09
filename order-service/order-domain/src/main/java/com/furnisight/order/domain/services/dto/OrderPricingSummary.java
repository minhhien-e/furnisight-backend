package com.furnisight.order.domain.services.dto;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderPricingSummary {
    double subtotal;
    double voucherDiscount;
    double shippingDiscount;
    double comboDiscount;
    double shippingFee;
    double insuranceFee;
    double totalAmount;

    public static OrderPricingSummary zero(double subtotal, Double shippingFee, Double insuranceFee) {
        double actualShippingFee = nonNegative(shippingFee);
        double actualInsuranceFee = nonNegative(insuranceFee);
        double total = Math.max(0.0, subtotal + actualShippingFee + actualInsuranceFee);
        return OrderPricingSummary.builder()
                .subtotal(subtotal)
                .shippingFee(actualShippingFee)
                .insuranceFee(actualInsuranceFee)
                .totalAmount(total)
                .build();
    }

    public Double savedAmount() {
        return voucherDiscount + shippingDiscount + comboDiscount;
    }

    private static double nonNegative(Double value) {
        if (value == null) {
            return 0.0;
        }
        if (value < 0.0) {
            throw new ValidationException(ErrorCode.INVALID_FEE);
        }
        return value;
    }
}

package com.furnisight.order.domain.valueobjects;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFee {
    private Double shippingFee;
    private Double shippingDiscount;
    private Double discountAmount;
    private Double comboDiscount;
    private Double insuranceFee;
    private String shopVoucherCode;
    private String shippingVoucherCode;
    private String comboId;

    @Builder
    public OrderFee(Double shippingFee, Double shippingDiscount, Double discountAmount, Double comboDiscount, Double insuranceFee, String shopVoucherCode, String shippingVoucherCode, String comboId) {
        if (shippingFee != null && shippingFee < 0) throw new ValidationException(ErrorCode.INVALID_FEE);
        if (insuranceFee != null && insuranceFee < 0) throw new ValidationException(ErrorCode.INVALID_FEE);
        if (shippingDiscount != null && shippingDiscount < 0) throw new ValidationException(ErrorCode.INVALID_DISCOUNT);
        if (discountAmount != null && discountAmount < 0) throw new ValidationException(ErrorCode.INVALID_DISCOUNT);
        if (comboDiscount != null && comboDiscount < 0) throw new ValidationException(ErrorCode.INVALID_DISCOUNT);

        this.shippingFee = shippingFee;
        this.shippingDiscount = shippingDiscount;
        this.discountAmount = discountAmount;
        this.comboDiscount = comboDiscount;
        this.insuranceFee = insuranceFee;
        this.shopVoucherCode = shopVoucherCode;
        this.shippingVoucherCode = shippingVoucherCode;
        this.comboId = comboId;
    }

    public Double calculateTotalFee() {
        double fee = 0.0;
        if (shippingFee != null) fee += shippingFee;
        if (insuranceFee != null) fee += insuranceFee;
        if (shippingDiscount != null) fee -= shippingDiscount;
        if (discountAmount != null) fee -= discountAmount;
        if (comboDiscount != null) fee -= comboDiscount;
        return Math.max(0.0, fee);
    }
}

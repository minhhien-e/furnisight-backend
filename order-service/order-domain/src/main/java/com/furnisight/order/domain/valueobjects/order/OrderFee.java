package com.furnisight.order.domain.valueobjects.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFee {
    private Double shippingFee;
    private Double shippingDiscount;
    private Double discountAmount;
    private Double insuranceFee;
}

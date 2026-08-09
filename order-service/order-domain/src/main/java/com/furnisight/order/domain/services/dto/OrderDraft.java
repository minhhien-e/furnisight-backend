package com.furnisight.order.domain.services.dto;

import com.furnisight.order.domain.valueobjects.PaymentDetail;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class OrderDraft {
    UUID userId;
    String customerEmail;
    String customerNote;
    OrderAddressInfo addressInfo;
    List<OrderItemParam> items;
    OrderPricingSummary pricingSummary;
    PaymentDetail paymentDetail;
    String shopVoucherCode;
    String shippingVoucherCode;
    String comboId;

    public PaymentDetail paymentDetailOrDefault() {
        if (paymentDetail != null) {
            return paymentDetail;
        }
        return PaymentDetail.builder()
                .paymentMethod(null)
                .paymentStatus("UNPAID")
                .paidAmount(0.0)
                .build();
    }
}

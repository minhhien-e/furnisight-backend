package com.furnisight.order.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentDetailResponse {
    private String paymentMethod;
    private String paymentStatus;
    private Double paidAmount;
    private LocalDateTime paidAt;
}

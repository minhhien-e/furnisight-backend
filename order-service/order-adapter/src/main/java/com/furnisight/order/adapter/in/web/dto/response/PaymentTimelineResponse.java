package com.furnisight.order.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentTimelineResponse {
    private LocalDateTime orderCreatedAt;
    private LocalDateTime paymentInitiatedAt;
    private LocalDateTime paymentCompletedAt;
    private LocalDateTime paymentFailedAt;
}

package com.furnisight.order.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
public class OrderDetailResponse {
    private UUID id;
    private String orderCode;
    private String status;
    private Double subTotal;
    private Double totalAmount;
    private Double savedAmount;
    private String customerNote;
    private Object fee;
    private Object shippingDetail;
    private PaymentDetailResponse paymentDetail;
    private PaymentTimelineResponse paymentTimeline;
    private List<?> items;
    private LocalDateTime createdAt;
    private LocalDateTime paymentExpiresAt;
    private Boolean canRetryPayment;
}

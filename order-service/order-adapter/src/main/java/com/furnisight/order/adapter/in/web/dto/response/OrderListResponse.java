package com.furnisight.order.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderListResponse {
    private UUID id;
    private String orderCode;
    private String status;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private String paymentMethod;
}

package com.furnisight.order.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderStatusHistoryResponse {
    private String previousStatus;
    private String nextStatus;
    private String actorType;
    private String trackingCode;
    private String note;
    private LocalDateTime createdAt;
}

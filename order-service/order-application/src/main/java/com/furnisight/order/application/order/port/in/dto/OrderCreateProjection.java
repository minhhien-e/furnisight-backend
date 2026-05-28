package com.furnisight.order.application.order.port.in.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderCreateProjection {
    private UUID orderId;
    private String orderCode;
    private String status;
}

package com.furnisight.order.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {
    private UUID id;
    private Object productSnapshot;
    private Double price;
    private Double oldPrice;
    private Integer quantity;
}

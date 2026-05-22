package com.furnisight.order.application.order.port.in.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryProjection {
    private UUID id;
    private String status;
    private Double totalAmount;
    private Double shippingFee;
    private Double discountAmount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;
    
    // First item or list of minimal items to show in the order list
    private List<OrderItemProjection> items;
}

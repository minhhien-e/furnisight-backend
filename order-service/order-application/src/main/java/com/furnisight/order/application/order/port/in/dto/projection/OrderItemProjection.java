package com.furnisight.order.application.order.port.in.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemProjection {
    private UUID id;
    private String productId;
    private String variantId;
    private String categoryName;
    private String productName;
    private String variantDescription;
    private Double price;
    private Integer quantity;
    private String imageUrl;
}

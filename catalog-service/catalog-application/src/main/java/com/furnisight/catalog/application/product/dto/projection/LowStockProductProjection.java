package com.furnisight.catalog.application.product.dto.projection;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LowStockProductProjection {
    private UUID id;
    private String name;
    private String categoryName;
    private Integer stock;
}

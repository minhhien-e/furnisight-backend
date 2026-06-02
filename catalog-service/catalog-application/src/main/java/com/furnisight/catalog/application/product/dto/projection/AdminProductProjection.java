package com.furnisight.catalog.application.product.dto.projection;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AdminProductProjection {
    private UUID id;
    private String name;
    private String slug;
    private String sku;
    private String categoryName;
    private Double price;
    private Integer stock;
    private String status;
    private String modelUrl;
    private List<String> imageUrls;
}

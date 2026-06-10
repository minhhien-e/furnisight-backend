package com.furnisight.catalog.application.product.dto.projection;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RecommendedProductProjection {
    private UUID id;
    private String slug;
    private String name;
    private String categoryName;
    private Double price;
    private String image;
    private String modelUrl;
    private UUID defaultVariantId;
    private Double rating;
    private Integer ratingCount;
    private Integer soldCount;
    private List<String> tags;
}

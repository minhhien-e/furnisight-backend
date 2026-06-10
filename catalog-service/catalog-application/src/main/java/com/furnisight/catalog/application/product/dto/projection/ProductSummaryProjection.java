package com.furnisight.catalog.application.product.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSummaryProjection {
    private UUID id;
    private String slug;
    private String name;
    private String categoryName;
    private Double price;
    private String image;
    private Double rating;
    private Integer ratingCount;
    private Integer soldCount;
    private List<String> tags;
}

package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import com.furnisight.catalog.domain.valueobjects.product.VariantSpecifications;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProductVariantRequest {
    private Double price;
    private Integer stockQuantity;
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    private String material;
    private String color;
    private String warranty;
    private String sku;
    private Integer lowStockThreshold;
    private Boolean supports3d;
    private java.util.UUID modelMediaId;
    private String modelUrl;
    private java.util.List<String> imageUrls;
    private VariantSpecifications specifications;
}

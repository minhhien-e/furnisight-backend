package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    private UUID shopId;
    private UUID categoryId;
    private String name;
    private String description;
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    private Map<String, Object> attributes;
    private List<VariantRequest> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantRequest {
        private String sku;
        private Double price;
        private Integer stockQuantity;
    }
}

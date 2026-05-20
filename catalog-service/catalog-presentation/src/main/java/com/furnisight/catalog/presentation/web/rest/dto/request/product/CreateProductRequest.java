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
    private UUID categoryId;
    private String name;
    private String slug;
    private String description;
    private Map<String, Object> attributes;
    private List<VariantRequest> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantRequest {
        private Double price;
        private Integer stockQuantity;
        private Double weight;
        private Double length;
        private Double width;
        private Double height;
    }
}

package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
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

    @Getter
    @Builder
    public static class VariantRequest {
        private String sku;
        private Double price;
        private Integer stockQuantity;
    }
}

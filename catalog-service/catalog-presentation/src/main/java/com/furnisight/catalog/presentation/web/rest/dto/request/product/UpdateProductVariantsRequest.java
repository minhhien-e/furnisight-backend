package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UpdateProductVariantsRequest {
    private UUID shopId;
    private List<VariantRequest> variants;

    @Getter
    @Builder
    public static class VariantRequest {
        private String sku;
        private Double price;
        private Integer stockQuantity;
    }
}

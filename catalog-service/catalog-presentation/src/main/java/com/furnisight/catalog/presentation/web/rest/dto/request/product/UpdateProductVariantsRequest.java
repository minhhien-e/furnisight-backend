package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductVariantsRequest {
    private UUID shopId;
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

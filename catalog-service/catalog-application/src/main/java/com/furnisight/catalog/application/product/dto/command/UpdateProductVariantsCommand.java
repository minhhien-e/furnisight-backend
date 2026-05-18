package com.furnisight.catalog.application.product.dto.command;

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
public class UpdateProductVariantsCommand {
    private UUID shopId;
    private UUID productId;
    private List<VariantCommand> variants;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VariantCommand {
        private String sku;
        private Double price;
        private Integer stockQuantity;
    }
}

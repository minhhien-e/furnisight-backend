package com.furnisight.catalog.application.product.dto;

import com.furnisight.catalog.domain.entities.product.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductCommand {
    private UUID shopId;
    private UUID categoryId; // ID cua Category (DDD: tham chieu bang ID)
    private String name;
    private String description;
    private Map<String, Object> attributes;
    private Double weight;
    private Double length;
    private Double height;
    private Double width;
    private List<VariantCommand> variants;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VariantCommand{
        private String sku;
        private Double price;
        private Integer stockQuantity;
    }
}

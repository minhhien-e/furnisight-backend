package com.furnisight.catalog.application.product.dto.command;

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
    private UUID categoryId; // ID cua Category (DDD: tham chieu bang ID)
    private String name;
    private String slug;
    private String description;
    private Map<String, Object> attributes;
    private List<VariantCommand> variants;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VariantCommand{
        private Double price;
        private Integer stockQuantity;
        private Double weight;
        private Double length;
        private Double width;
        private Double height;
    }
}

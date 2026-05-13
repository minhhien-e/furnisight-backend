package com.furnisight.catalog.application.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductInfoCommand {
    private UUID shopId;
    private UUID productId;
    private String name;
    private String description;
    private Map<String, Object> attributes;
    private Double weight;
    private Double length;
    private Double height;
    private Double width;
}

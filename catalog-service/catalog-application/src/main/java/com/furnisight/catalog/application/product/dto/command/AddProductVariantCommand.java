package com.furnisight.catalog.application.product.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddProductVariantCommand {
    private UUID productId;
    private Double price;
    private Integer stockQuantity;
    private Double weight;
    private Double length;   // cm, required
    private Double width;    // cm, required
    private Double height;   // cm, required
    private String material; // required
    private String warranty; // optional
    private String color;
    private String sku;
    private Integer lowStockThreshold;
    private UUID modelMediaId;
    private String modelUrl;
    private Boolean supports3d;
}

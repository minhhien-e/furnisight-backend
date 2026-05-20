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
    private Double length;
    private Double width;
    private Double height;
}

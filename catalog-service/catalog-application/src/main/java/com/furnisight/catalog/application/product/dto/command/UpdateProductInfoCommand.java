package com.furnisight.catalog.application.product.dto.command;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductInfoCommand {
    private UUID productId;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private List<String> features;
}

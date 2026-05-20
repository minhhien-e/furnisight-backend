package com.furnisight.catalog.application.product.dto.command;

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
    private UUID productId;
    private String name;
    private String slug;
    private String description;
    private Map<String, Object> attributes;
    private Map<String, Object> metadata;
    private Map<String, String> specs;
}

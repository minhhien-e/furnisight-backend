package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductInfoRequest {
    private String name;
    private String slug;
    private String description;
    private Map<String, Object> attributes;
    private Map<String, Object> metadata;
    private Map<String, String> specs;
}

package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductInfoRequest {
    private UUID shopId;
    private String name;
    private String description;
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    private Map<String, Object> attributes;
}

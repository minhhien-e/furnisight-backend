package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductInfoRequest {
    private String name;
    private String slug;
    private String description;
    private List<String> features;
    private Boolean supports3d;
    private String modelUrl;
}

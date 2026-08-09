package com.furnisight.catalog.presentation.web.rest.dto.request.category;

import com.furnisight.catalog.domain.valueobjects.product.VariantSpecifications;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    private String name;
    private String slug;
    private UUID parentId;
    private UUID roomTypeId;
    private String iconId;
    private Boolean visible;
    private String description;
    private String imageUrl;
    private VariantSpecifications specTemplate;
}

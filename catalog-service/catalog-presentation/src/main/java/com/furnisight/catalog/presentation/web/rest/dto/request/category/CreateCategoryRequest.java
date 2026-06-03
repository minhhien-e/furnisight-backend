package com.furnisight.catalog.presentation.web.rest.dto.request.category;

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
    private String iconId;
    private Boolean visible;
    private String description;
    private String imageUrl;
}

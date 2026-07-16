package com.furnisight.catalog.application.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;
    private String path;
    private Integer productCount;
    private Boolean visible;
    private String description;
    private String imageUrl;
    private String iconUrl;
}

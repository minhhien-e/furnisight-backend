package com.furnisight.catalog.application.category.dto.projection;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CategoryDetailProjection {
    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;
    private String path;
    private Integer productCount;
    private String imageUrl;
    private String iconUrl;
}

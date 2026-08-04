package com.furnisight.catalog.application.category.dto.response;

import com.furnisight.catalog.domain.valueobjects.product.VariantSpecifications;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse implements java.io.Serializable {
    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;
    private UUID roomTypeId;
    private String path;
    private Integer productCount;
    private Boolean visible;
    private String description;
    private String imageUrl;
    private String iconUrl;
    private VariantSpecifications specTemplate;
    private java.time.LocalDateTime createdAt;
}

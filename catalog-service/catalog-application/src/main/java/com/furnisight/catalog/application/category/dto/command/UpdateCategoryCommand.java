package com.furnisight.catalog.application.category.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryCommand {
    private UUID categoryId;
    private String name;
    private String slug;
    private UUID parentId;
    private String iconId;
    private Boolean visible;
    private String description;
    private String imageUrl;
    private UUID roomTypeId;
}

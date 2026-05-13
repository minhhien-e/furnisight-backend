package com.furnisight.catalog.application.category.dto;

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
}

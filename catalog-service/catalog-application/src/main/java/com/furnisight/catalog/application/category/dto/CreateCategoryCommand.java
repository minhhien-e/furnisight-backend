package com.furnisight.catalog.application.category.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryCommand {
    private String name;
    private String slug;
    private UUID parentId;
}

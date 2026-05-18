package com.furnisight.catalog.presentation.web.rest.dto.request.category;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    private String name;
    private String slug;
    private UUID parentId;
}

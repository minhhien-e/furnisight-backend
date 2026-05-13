package com.furnisight.catalog.presentation.web.rest.dto.request.category;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CreateCategoryRequest {
    private String name;
    private String slug;
    private UUID parentId;
}

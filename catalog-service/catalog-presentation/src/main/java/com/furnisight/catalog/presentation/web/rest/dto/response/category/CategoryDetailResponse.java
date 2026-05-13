package com.furnisight.catalog.presentation.web.rest.dto.response.category;

import com.furnisight.catalog.application.category.dto.CategoryDetailResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CategoryDetailResponse {
    private UUID id;
    private String name;
    private String slug;
    private String path;
    private UUID parentId;

    public static CategoryDetailResponse from(CategoryDetailResponseDto dto) {
        return CategoryDetailResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .slug(dto.getSlug())
                .path(dto.getPath())
                .parentId(dto.getParentId())
                .build();
    }
}

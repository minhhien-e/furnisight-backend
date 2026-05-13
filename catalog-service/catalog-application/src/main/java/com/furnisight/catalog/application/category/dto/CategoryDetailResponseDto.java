package com.furnisight.catalog.application.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CategoryDetailResponseDto {
    private UUID id;
    private String name;
    private String slug;
    private UUID parantId;
    private String path;
}

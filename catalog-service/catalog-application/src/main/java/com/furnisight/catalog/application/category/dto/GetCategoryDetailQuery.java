package com.furnisight.catalog.application.category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GetCategoryDetailQuery {
    private UUID categoryId;
}

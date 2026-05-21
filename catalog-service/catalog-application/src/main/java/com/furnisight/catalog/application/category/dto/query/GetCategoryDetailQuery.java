package com.furnisight.catalog.application.category.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GetCategoryDetailQuery {
    private String slug;
}

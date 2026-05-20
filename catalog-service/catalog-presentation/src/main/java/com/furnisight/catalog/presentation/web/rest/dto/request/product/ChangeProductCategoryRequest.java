package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeProductCategoryRequest {
    private UUID categoryId;
}

package com.furnisight.catalog.presentation.web.rest.dto.request.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCollectionRequest {
    private String name;
    private String description;
    private String slug;
}

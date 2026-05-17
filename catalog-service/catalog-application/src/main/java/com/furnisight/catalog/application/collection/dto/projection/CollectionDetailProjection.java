package com.furnisight.catalog.application.collection.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionDetailProjection {
    private UUID id;
    private String name;
    private String description;
    private String slug;
}

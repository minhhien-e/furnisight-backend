package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignProductCollectionRequest {
    private UUID collectionId;
}

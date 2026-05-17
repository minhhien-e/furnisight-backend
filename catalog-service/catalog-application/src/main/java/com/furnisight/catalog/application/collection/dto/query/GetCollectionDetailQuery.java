package com.furnisight.catalog.application.collection.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetCollectionDetailQuery {
    private UUID collectionId;
}

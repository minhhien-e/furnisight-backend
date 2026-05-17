package com.furnisight.catalog.application.collection.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCollectionCommand {
    private UUID collectionId;
    private String name;
    private String description;
    private String slug;
}

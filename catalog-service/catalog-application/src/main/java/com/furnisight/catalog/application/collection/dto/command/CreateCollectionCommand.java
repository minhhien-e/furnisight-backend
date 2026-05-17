package com.furnisight.catalog.application.collection.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCollectionCommand {
    private String name;
    private String description;
    private String slug;
}

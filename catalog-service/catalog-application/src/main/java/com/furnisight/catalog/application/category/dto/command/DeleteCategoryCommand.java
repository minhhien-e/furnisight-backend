package com.furnisight.catalog.application.category.dto.command;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DeleteCategoryCommand(UUID categoryId) {
}

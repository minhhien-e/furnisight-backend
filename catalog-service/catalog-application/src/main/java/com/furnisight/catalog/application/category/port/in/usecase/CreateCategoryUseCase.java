package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.command.CreateCategoryCommand;

public interface CreateCategoryUseCase {
    void execute(CreateCategoryCommand command);
}

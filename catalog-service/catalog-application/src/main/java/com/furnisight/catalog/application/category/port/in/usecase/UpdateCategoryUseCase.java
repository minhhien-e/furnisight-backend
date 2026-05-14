package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.command.UpdateCategoryCommand;

public interface UpdateCategoryUseCase {
    void execute(UpdateCategoryCommand command);
}

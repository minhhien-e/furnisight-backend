package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.command.DeleteCategoryCommand;

public interface DeleteCategoryUseCase {
    void execute(DeleteCategoryCommand command);
}

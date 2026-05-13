package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.UpdateCategoryCommand;

public interface UpdateCategoryUseCase {
    void execute(UpdateCategoryCommand command);
}

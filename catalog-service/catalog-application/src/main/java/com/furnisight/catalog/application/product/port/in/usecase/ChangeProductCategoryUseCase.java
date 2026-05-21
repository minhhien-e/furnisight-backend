package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.ChangeProductCategoryCommand;

public interface ChangeProductCategoryUseCase {
    void execute(ChangeProductCategoryCommand command);
}

package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.AddProductVariantCommand;

public interface AddProductVariantUseCase {
    void execute(AddProductVariantCommand command);
}

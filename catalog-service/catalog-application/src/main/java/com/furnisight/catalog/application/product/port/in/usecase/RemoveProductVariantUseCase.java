package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.RemoveProductVariantCommand;

public interface RemoveProductVariantUseCase {
    void execute(RemoveProductVariantCommand command);
}

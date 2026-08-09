package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.UpdateProductStatusCommand;

public interface UpdateProductStatusUseCase {
    void execute(UpdateProductStatusCommand command);
}

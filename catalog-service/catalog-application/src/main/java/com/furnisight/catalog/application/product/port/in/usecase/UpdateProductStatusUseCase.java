package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.UpdateProductStatusCommand;

public interface UpdateProductStatusUseCase {
    void execute(UpdateProductStatusCommand command);
}

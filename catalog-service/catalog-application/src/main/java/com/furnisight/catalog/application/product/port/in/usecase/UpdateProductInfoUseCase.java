package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.UpdateProductInfoCommand;

public interface UpdateProductInfoUseCase {
    void execute(UpdateProductInfoCommand command);
}

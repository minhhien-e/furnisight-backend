package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.UpdateProductInfoCommand;

public interface UpdateProductInfoUseCase {
    void execute(UpdateProductInfoCommand command);
}

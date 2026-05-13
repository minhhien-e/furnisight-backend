package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.UpdateProductVariantsCommand;

public interface UpdateProductVariantsUseCase {
    void execute(UpdateProductVariantsCommand command);
}

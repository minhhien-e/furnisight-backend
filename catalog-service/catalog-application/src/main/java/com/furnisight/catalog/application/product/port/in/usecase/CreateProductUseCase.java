package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.CreateProductCommand;

public interface CreateProductUseCase {
    void execute(CreateProductCommand command);
}

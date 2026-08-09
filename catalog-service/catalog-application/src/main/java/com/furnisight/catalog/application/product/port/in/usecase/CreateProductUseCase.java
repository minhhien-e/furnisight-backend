package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.CreateProductCommand;

public interface CreateProductUseCase {
    void execute(CreateProductCommand command);
}

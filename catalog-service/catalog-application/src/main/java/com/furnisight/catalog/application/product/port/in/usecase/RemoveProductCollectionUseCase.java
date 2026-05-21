package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.RemoveProductCollectionCommand;

public interface RemoveProductCollectionUseCase {
    void execute(RemoveProductCollectionCommand command);
}

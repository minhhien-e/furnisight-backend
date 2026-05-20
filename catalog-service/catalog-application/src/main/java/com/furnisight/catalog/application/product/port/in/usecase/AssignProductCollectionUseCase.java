package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.command.AssignProductCollectionCommand;

public interface AssignProductCollectionUseCase {
    void execute(AssignProductCollectionCommand command);
}

package com.furnisight.catalog.application.collection.port.in.usecase;

import com.furnisight.catalog.application.collection.dto.command.CreateCollectionCommand;

public interface CreateCollectionUseCase {
    void execute(CreateCollectionCommand command);
}

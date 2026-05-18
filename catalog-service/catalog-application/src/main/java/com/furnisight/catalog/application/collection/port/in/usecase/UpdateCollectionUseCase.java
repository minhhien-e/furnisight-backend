package com.furnisight.catalog.application.collection.port.in.usecase;

import com.furnisight.catalog.application.collection.dto.command.UpdateCollectionCommand;

public interface UpdateCollectionUseCase {
    void execute(UpdateCollectionCommand command);
}

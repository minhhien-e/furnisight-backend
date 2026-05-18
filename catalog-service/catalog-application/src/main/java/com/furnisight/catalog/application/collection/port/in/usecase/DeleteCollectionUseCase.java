package com.furnisight.catalog.application.collection.port.in.usecase;

import java.util.UUID;

public interface DeleteCollectionUseCase {
    void execute(UUID collectionId);
}

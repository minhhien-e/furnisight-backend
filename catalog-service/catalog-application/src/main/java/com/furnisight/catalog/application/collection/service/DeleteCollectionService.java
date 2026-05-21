package com.furnisight.catalog.application.collection.service;

import com.furnisight.catalog.application.collection.port.in.usecase.DeleteCollectionUseCase;
import com.furnisight.catalog.domain.repository.CollectionRepository;
import com.furnisight.catalog.domain.entities.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCollectionService implements DeleteCollectionUseCase {
    private final CollectionRepository collectionRepository;

    @Override
    @Transactional
    public void execute(UUID collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found: " + collectionId));
        collectionRepository.delete(collection);
    }
}

package com.furnisight.catalog.application.collection.service;

import com.furnisight.catalog.application.collection.dto.command.UpdateCollectionCommand;
import com.furnisight.catalog.application.collection.port.in.usecase.UpdateCollectionUseCase;
import com.furnisight.catalog.domain.repository.CollectionRepository;
import com.furnisight.catalog.domain.entities.collection.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCollectionService implements UpdateCollectionUseCase {
    private final CollectionRepository collectionRepository;

    @Override
    @Transactional
    public void execute(UpdateCollectionCommand command) {
        Collection collection = collectionRepository.findById(command.getCollectionId())
                .orElseThrow(() -> new IllegalArgumentException("Collection not found: " + command.getCollectionId()));

        if (command.getName() != null) {
            collection.setName(command.getName());
        }
        if (command.getDescription() != null) {
            collection.setDescription(command.getDescription());
        }
        if (command.getSlug() != null) {
            collection.setSlug(command.getSlug());
        }

        collectionRepository.save(collection);
    }
}

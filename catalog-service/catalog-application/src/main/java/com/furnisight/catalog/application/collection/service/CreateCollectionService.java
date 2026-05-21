package com.furnisight.catalog.application.collection.service;

import com.furnisight.catalog.application.collection.dto.command.CreateCollectionCommand;
import com.furnisight.catalog.application.collection.port.in.usecase.CreateCollectionUseCase;
import com.furnisight.catalog.domain.repository.CollectionRepository;
import com.furnisight.catalog.domain.entities.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCollectionService implements CreateCollectionUseCase {
    private final CollectionRepository collectionRepository;

    @Override
    @Transactional
    public void execute(CreateCollectionCommand command) {
        Collection collection = Collection.builder()
                .name(command.getName())
                .description(command.getDescription())
                .slug(command.getSlug())
                .build();
        collectionRepository.save(collection);
    }
}

package com.furnisight.catalog.application.collection.service;

import com.furnisight.catalog.application.collection.dto.projection.CollectionDetailProjection;
import com.furnisight.catalog.application.collection.port.in.usecase.ListCollectionsUseCase;
import com.furnisight.catalog.application.collection.port.out.CollectionReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCollectionsService implements ListCollectionsUseCase {
    private final CollectionReadRepository collectionReadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CollectionDetailProjection> execute() {
        return collectionReadRepository.findAllCollections();
    }
}

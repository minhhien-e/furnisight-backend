package com.furnisight.catalog.application.collection.service;

import com.furnisight.catalog.application.collection.dto.projection.CollectionDetailProjection;
import com.furnisight.catalog.application.collection.port.in.usecase.ListCollectionsUseCase;
import com.furnisight.catalog.domain.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListCollectionsService implements ListCollectionsUseCase {
    private final CollectionRepository collectionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CollectionDetailProjection> execute() {
        return collectionRepository.findAll().stream()
                .map(collection -> CollectionDetailProjection.builder()
                        .id(collection.getId())
                        .name(collection.getName())
                        .description(collection.getDescription())
                        .slug(collection.getSlug())
                        .build())
                .collect(Collectors.toList());
    }
}

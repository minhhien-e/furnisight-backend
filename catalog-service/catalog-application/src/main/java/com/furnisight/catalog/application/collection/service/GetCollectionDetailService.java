package com.furnisight.catalog.application.collection.service;

import com.furnisight.catalog.application.collection.dto.projection.CollectionDetailProjection;
import com.furnisight.catalog.application.collection.dto.query.GetCollectionDetailQuery;
import com.furnisight.catalog.application.collection.port.in.usecase.GetCollectionDetailUseCase;
import com.furnisight.catalog.domain.repository.CollectionRepository;
import com.furnisight.catalog.domain.entities.collection.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCollectionDetailService implements GetCollectionDetailUseCase {
    private final CollectionRepository collectionRepository;

    @Override
    @Transactional(readOnly = true)
    public CollectionDetailProjection execute(GetCollectionDetailQuery query) {
        Collection collection = collectionRepository.findById(query.getCollectionId())
                .orElseThrow(() -> new IllegalArgumentException("Collection not found: " + query.getCollectionId()));

        return CollectionDetailProjection.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .slug(collection.getSlug())
                .build();
    }
}

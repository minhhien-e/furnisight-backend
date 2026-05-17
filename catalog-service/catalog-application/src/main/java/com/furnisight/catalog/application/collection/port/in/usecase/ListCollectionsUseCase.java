package com.furnisight.catalog.application.collection.port.in.usecase;

import com.furnisight.catalog.application.collection.dto.projection.CollectionDetailProjection;

import java.util.List;

public interface ListCollectionsUseCase {
    List<CollectionDetailProjection> execute();
}

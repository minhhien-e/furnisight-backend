package com.furnisight.catalog.application.collection.port.in.usecase;

import com.furnisight.catalog.application.collection.dto.projection.CollectionDetailProjection;
import com.furnisight.catalog.application.collection.dto.query.GetCollectionDetailQuery;

public interface GetCollectionDetailUseCase {
    CollectionDetailProjection execute(GetCollectionDetailQuery query);
}

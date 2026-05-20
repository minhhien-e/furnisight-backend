package com.furnisight.catalog.application.collection.port.out;

import com.furnisight.catalog.application.collection.dto.projection.CollectionDetailProjection;
import java.util.List;

public interface CollectionReadRepository {
    List<CollectionDetailProjection> findAllCollections();
}

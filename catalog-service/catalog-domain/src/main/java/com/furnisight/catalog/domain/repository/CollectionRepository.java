package com.furnisight.catalog.domain.repository;

import com.furnisight.catalog.domain.entities.collection.Collection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository {
    Optional<Collection> findById(UUID id);
    Optional<Collection> findBySlug(String slug);
    Collection save(Collection collection);
    void delete(Collection collection);
    List<Collection> findAll();
}

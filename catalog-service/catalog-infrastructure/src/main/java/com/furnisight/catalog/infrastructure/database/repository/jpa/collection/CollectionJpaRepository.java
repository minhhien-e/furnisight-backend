package com.furnisight.catalog.infrastructure.database.repository.jpa.collection;

import com.furnisight.catalog.domain.entities.collection.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CollectionJpaRepository extends JpaRepository<Collection, UUID> {
    Optional<Collection> findBySlug(String slug);
}

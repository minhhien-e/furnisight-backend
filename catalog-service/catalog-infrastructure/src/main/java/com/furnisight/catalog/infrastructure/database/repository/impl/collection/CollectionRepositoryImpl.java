package com.furnisight.catalog.infrastructure.database.repository.impl.collection;

import com.furnisight.catalog.infrastructure.database.repository.jpa.collection.CollectionJpaRepository;
import com.furnisight.catalog.domain.repository.CollectionRepository;
import com.furnisight.catalog.domain.entities.collection.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CollectionRepositoryImpl implements CollectionRepository {

    private final CollectionJpaRepository jpaCollectionRepository;

    @Override
    public Optional<Collection> findById(UUID id) {
        return jpaCollectionRepository.findById(id);
    }

    @Override
    public Optional<Collection> findBySlug(String slug) {
        return jpaCollectionRepository.findBySlug(slug);
    }

    @Override
    public Collection save(Collection collection) {
        return jpaCollectionRepository.save(collection);
    }

    @Override
    public void delete(Collection collection) {
        jpaCollectionRepository.delete(collection);
    }

    @Override
    public List<Collection> findAll() {
        return jpaCollectionRepository.findAll();
    }
}

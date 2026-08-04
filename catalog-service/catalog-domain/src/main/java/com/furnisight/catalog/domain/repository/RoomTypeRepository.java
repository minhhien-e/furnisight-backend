package com.furnisight.catalog.domain.repository;

import com.furnisight.catalog.domain.entities.RoomType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomTypeRepository {
    Optional<RoomType> findById(UUID id);
    Optional<RoomType> findBySlug(String slug);
    List<RoomType> findAll();
    List<RoomType> findByVisibleTrue();
    RoomType save(RoomType roomType);
    void deleteById(UUID id);
    boolean existsBySlug(String slug);
}

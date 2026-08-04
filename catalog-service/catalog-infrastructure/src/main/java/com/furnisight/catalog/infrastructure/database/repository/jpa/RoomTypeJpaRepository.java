package com.furnisight.catalog.infrastructure.database.repository.jpa;

import com.furnisight.catalog.domain.entities.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomTypeJpaRepository extends JpaRepository<RoomType, UUID> {
    Optional<RoomType> findBySlug(String slug);
    List<RoomType> findByVisibleTrue();
    boolean existsBySlug(String slug);
}

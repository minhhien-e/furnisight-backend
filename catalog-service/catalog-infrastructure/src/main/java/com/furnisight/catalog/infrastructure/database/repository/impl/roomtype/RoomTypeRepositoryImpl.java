package com.furnisight.catalog.infrastructure.database.repository.impl.roomtype;

import com.furnisight.catalog.domain.entities.RoomType;
import com.furnisight.catalog.domain.repository.RoomTypeRepository;
import com.furnisight.catalog.infrastructure.database.repository.jpa.RoomTypeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RoomTypeRepositoryImpl implements RoomTypeRepository {

    private final RoomTypeJpaRepository jpaRepository;

    @Override
    public Optional<RoomType> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<RoomType> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug);
    }

    @Override
    public List<RoomType> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<RoomType> findByVisibleTrue() {
        return jpaRepository.findByVisibleTrue();
    }

    @Override
    public RoomType save(RoomType roomType) {
        return jpaRepository.save(roomType);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }
}

package com.furnisight.catalog.infrastructure.database.repository.jpa;

import com.furnisight.catalog.domain.entities.ProductFavoriteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProductFavoriteLogJpaRepository extends JpaRepository<ProductFavoriteLog, UUID> {
}

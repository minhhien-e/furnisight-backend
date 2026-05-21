package com.furnisight.catalog.domain.repository;

import com.furnisight.catalog.domain.entities.ProductFavoriteLog;

public interface ProductFavoriteLogRepository {
    void save(ProductFavoriteLog log);
}

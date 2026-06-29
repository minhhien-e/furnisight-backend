package com.furnisight.user.application.favorite.port.out;

import com.furnisight.user.application.favorite.dto.CatalogFavoriteProductSummary;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface CatalogFavoriteProductService {
    Map<UUID, CatalogFavoriteProductSummary> getFavoriteProductSummaries(Collection<UUID> productIds, String locale);
}

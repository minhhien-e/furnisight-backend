package com.furnisight.user.application.favorite.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.favorite.dto.FavoriteProductProjection;

import java.util.List;
import java.util.UUID;

public interface GetFavoriteProductsUseCase extends UseCase<UUID, List<FavoriteProductProjection>> {
}

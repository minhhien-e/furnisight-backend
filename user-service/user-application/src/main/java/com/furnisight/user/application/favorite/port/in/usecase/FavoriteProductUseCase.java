package com.furnisight.user.application.favorite.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.favorite.dto.FavoriteProductCommand;
import com.furnisight.user.application.favorite.dto.FavoriteProductProjection;

public interface FavoriteProductUseCase extends UseCase<FavoriteProductCommand, FavoriteProductProjection> {
}

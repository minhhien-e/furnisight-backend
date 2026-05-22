package com.furnisight.user.application.favorite.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.favorite.dto.FavoriteProductCommand;

public interface UnfavoriteProductUseCase extends UseCase<FavoriteProductCommand, Void> {
}

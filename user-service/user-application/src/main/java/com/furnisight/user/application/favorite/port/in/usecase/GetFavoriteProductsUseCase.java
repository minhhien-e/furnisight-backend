package com.furnisight.user.application.favorite.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.favorite.dto.GetFavoriteProductsQuery;
import com.furnisight.user.application.favorite.dto.FavoriteProductResponse;

import java.util.List;

public interface GetFavoriteProductsUseCase extends UseCase<GetFavoriteProductsQuery, List<FavoriteProductResponse>> {
}

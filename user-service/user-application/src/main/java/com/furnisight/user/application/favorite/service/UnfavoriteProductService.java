package com.furnisight.user.application.favorite.service;

import com.furnisight.user.application.favorite.dto.FavoriteProductCommand;
import com.furnisight.user.application.favorite.port.in.usecase.UnfavoriteProductUseCase;
import com.furnisight.user.domain.repository.favorite.FavoriteProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnfavoriteProductService implements UnfavoriteProductUseCase {
    private final FavoriteProductRepository favoriteProductRepository;

    @Override
    @Transactional
    public Void execute(FavoriteProductCommand command) {
        favoriteProductRepository
            .findByAccountIdAndProductId(command.accountId(), command.productId())
            .ifPresent(favorite -> {
                favorite.unfavorite();
                favoriteProductRepository.delete(favorite);
            });
        return null;
    }
}

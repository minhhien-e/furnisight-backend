package com.furnisight.user.presentation.web.rest.controller.profile;

import com.furnisight.user.application.common.port.in.CurrentUserProvider;
import com.furnisight.user.application.favorite.dto.FavoriteProductCommand;
import com.furnisight.user.application.favorite.port.in.usecase.FavoriteProductUseCase;
import com.furnisight.user.application.favorite.port.in.usecase.GetFavoriteProductsUseCase;
import com.furnisight.user.presentation.web.rest.dto.response.FavoriteProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import com.furnisight.user.application.favorite.port.in.usecase.UnfavoriteProductUseCase;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/favorites/products")
@RequiredArgsConstructor
public class FavoriteProductController {
    private final FavoriteProductUseCase favoriteProductUseCase;
    private final UnfavoriteProductUseCase unfavoriteProductUseCase;
    private final GetFavoriteProductsUseCase getFavoriteProductsUseCase;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<List<FavoriteProductResponse>> getFavoriteProducts() {
        UUID accountId = currentUserProvider.getCurrentUserId();
        List<FavoriteProductResponse> favorites = getFavoriteProductsUseCase.execute(accountId).stream()
            .map(FavoriteProductResponse::from)
            .toList();
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<FavoriteProductResponse> favoriteProduct(@PathVariable UUID productId) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        var favorite = favoriteProductUseCase.execute(new FavoriteProductCommand(accountId, productId));
        return ResponseEntity.ok(FavoriteProductResponse.from(favorite));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> unfavoriteProduct(@PathVariable UUID productId) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        unfavoriteProductUseCase.execute(new FavoriteProductCommand(accountId, productId));
        return ResponseEntity.noContent().build();
    }
}

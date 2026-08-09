package com.furnisight.user.presentation.web.rest.controller.profile;

import com.furnisight.user.application.common.port.in.CurrentUserProvider;
import com.furnisight.user.application.favorite.dto.FavoriteProductCommand;
import com.furnisight.user.application.favorite.port.in.usecase.FavoriteProductUseCase;
import com.furnisight.user.application.favorite.port.in.usecase.GetFavoriteProductsUseCase;
import com.furnisight.user.presentation.web.rest.dto.response.FavoriteProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import com.furnisight.user.application.favorite.port.in.usecase.UnfavoriteProductUseCase;
import com.furnisight.user.application.favorite.dto.GetFavoriteProductsQuery;
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
    public ResponseEntity<List<FavoriteProductResponse>> getFavoriteProducts(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        String locale = resolveLocale(lang, acceptLanguage);
        List<FavoriteProductResponse> favorites = getFavoriteProductsUseCase
            .execute(new GetFavoriteProductsQuery(accountId, locale)).stream()
            .map(FavoriteProductResponse::from)
            .toList();
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<FavoriteProductResponse> favoriteProduct(
            @PathVariable UUID productId,
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        var favorite = favoriteProductUseCase.execute(
                new FavoriteProductCommand(accountId, productId, resolveLocale(lang, acceptLanguage))
        );
        return ResponseEntity.ok(FavoriteProductResponse.from(favorite));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> unfavoriteProduct(@PathVariable UUID productId) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        unfavoriteProductUseCase.execute(new FavoriteProductCommand(accountId, productId, "vi"));
        return ResponseEntity.noContent().build();
    }

    private String resolveLocale(String lang, String acceptLanguage) {
        String candidate = lang != null && !lang.isBlank() ? lang : acceptLanguage;
        if (candidate == null || candidate.isBlank()) {
            return "vi";
        }
        return candidate.trim().toLowerCase().startsWith("en") ? "en" : "vi";
    }
}

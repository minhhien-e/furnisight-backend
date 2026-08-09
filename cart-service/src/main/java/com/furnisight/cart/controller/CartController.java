package com.furnisight.cart.controller;

import com.furnisight.cart.dto.AddToCartRequest;
import com.furnisight.cart.dto.CartResponse;
import com.furnisight.cart.dto.UpdateCartItemRequest;
import com.furnisight.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("x-user-id") String userIdStr,
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang) {
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(cartService.getCart(userId, resolveLocale(lang, acceptLanguage)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @RequestHeader("x-user-id") String userIdStr,
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang,
            @Valid @RequestBody AddToCartRequest request) {
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(cartService.addToCart(userId, request, resolveLocale(lang, acceptLanguage)));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestHeader("x-user-id") String userIdStr,
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @PathVariable String productId,
            @RequestParam(name = "lang", required = false) String lang,
            @RequestParam(required = false) String variantId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(cartService.updateCartItem(
                userId,
                productId,
                variantId,
                request,
                resolveLocale(lang, acceptLanguage)
        ));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @RequestHeader("x-user-id") String userIdStr,
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @PathVariable String productId,
            @RequestParam(name = "lang", required = false) String lang,
            @RequestParam(required = false) String variantId) {
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(cartService.removeCartItem(
                userId,
                productId,
                variantId,
                resolveLocale(lang, acceptLanguage)
        ));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("x-user-id") String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        cartService.clearCart(userId);
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

package com.furnisight.cart.controller;

import com.furnisight.cart.dto.AddToCartRequest;
import com.furnisight.cart.dto.CartResponse;
import com.furnisight.cart.dto.UpdateCartItemRequest;
import com.furnisight.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddToCartRequest request) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(cartService.addToCart(userId, request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId,
            @RequestParam(required = false) String variantId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(cartService.updateCartItem(userId, productId, variantId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId,
            @RequestParam(required = false) String variantId) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(cartService.removeCartItem(userId, productId, variantId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}

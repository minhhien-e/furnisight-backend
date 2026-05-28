package com.furnisight.cart.controller;

import com.furnisight.cart.dto.AddToCartRequest;
import com.furnisight.cart.dto.CartResponse;
import com.furnisight.cart.dto.UpdateCartItemRequest;
import com.furnisight.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestHeader("x-user-id") String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @RequestHeader("x-user-id") String userIdStr,
            @Valid @RequestBody AddToCartRequest request) {
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(cartService.addToCart(userId, request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestHeader("x-user-id") String userIdStr,
            @PathVariable String productId,
            @RequestParam(required = false) String variantId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(cartService.updateCartItem(userId, productId, variantId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @RequestHeader("x-user-id") String userIdStr,
            @PathVariable String productId,
            @RequestParam(required = false) String variantId) {
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(cartService.removeCartItem(userId, productId, variantId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("x-user-id") String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}

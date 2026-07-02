package com.furnisight.cart.service;

import com.furnisight.cart.utils.CartUtils;
import com.furnisight.cart.dto.AddToCartRequest;
import com.furnisight.cart.dto.CartItemResponse;
import com.furnisight.cart.dto.CartItemVariantResponse;
import com.furnisight.cart.dto.CartResponse;
import com.furnisight.cart.dto.UpdateCartItemRequest;
import com.furnisight.cart.model.Cart;
import com.furnisight.cart.model.CartItem;
import com.furnisight.cart.model.CartItemVariant;
import com.furnisight.cart.policy.CartEnrichmentPolicy;
import com.furnisight.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartEnrichmentPolicy cartEnrichmentPolicy;

    public CartResponse getCart(UUID userId, String locale) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cartEnrichmentPolicy.enrichCart(cart, locale));
    }

    public CartResponse addToCart(UUID userId, AddToCartRequest request, String locale) {
        Cart cart = getOrCreateCart(userId);

        Optional<CartItem> existingItem = findItem(
                cart,
                request.getProductId(),
                request.getVariantId()
        );

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            cart.getItems().add(CartItem.builder()
                    .productId(request.getProductId())
                    .variantId(request.getVariantId())
                    .name(request.getName())
                    .price(request.getPrice())
                    .imageUrl(request.getImageUrl())
                    .quantity(request.getQuantity())
                    .build());
        }

        return saveAndRespond(cart, locale);
    }

    public CartResponse updateCartItem(
            UUID userId,
            String productId,
            String variantId,
            UpdateCartItemRequest request,
            String locale
    ) {
        Cart cart = getOrCreateCart(userId);

        findItem(cart, productId, variantId)
                .ifPresent(item -> updateExistingItem(cart, item, request));

        return saveAndRespond(cart, locale);
    }

    public CartResponse removeCartItem(UUID userId, String productId, String variantId, String locale) {
        Cart cart = getOrCreateCart(userId);

        cart.getItems().removeIf(item -> CartUtils.isSameItem(item, productId, variantId));

        return saveAndRespond(cart, locale);
    }

    public void clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        saveCart(cart);
    }

    private CartResponse saveAndRespond(Cart cart, String locale) {
        Cart savedCart = saveCart(cart);
        Cart enrichedCart = cartEnrichmentPolicy.enrichCart(savedCart, locale);
        clampCartQuantities(enrichedCart);
        CartResponse response = toResponse(enrichedCart); // build response trước khi save lần 2
        saveCart(enrichedCart);                            // persist clamped quantities, bỏ qua return value
        return response;
    }

    private Cart saveCart(Cart cart) {
        CartUtils.updateCartTotal(cart);
        return cartRepository.save(cart);
    }

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findById(userId)
                .orElseGet(() -> createEmptyCart(userId));
    }

    private Cart createEmptyCart(UUID userId) {
        return Cart.builder()
                .id(userId)
                .items(new ArrayList<>())
                .total(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Optional<CartItem> findItem(Cart cart, String productId, String variantId) {
        return cart.getItems().stream()
                .filter(item -> CartUtils.isSameItem(item, productId, variantId))
                .findFirst();
    }

    private void updateExistingItem(Cart cart, CartItem item, UpdateCartItemRequest request) {
        String nextVariantId = CartUtils.normalizeNullable(request.getVariantId());
        String currentVariantId = CartUtils.normalizeNullable(item.getVariantId());

        item.setQuantity(request.getQuantity());

        if (CartUtils.equalsNullable(currentVariantId, nextVariantId)) {
            return;
        }

        Optional<CartItem> targetItem = findItem(cart, item.getProductId(), nextVariantId)
                .filter(existing -> existing != item);

        if (targetItem.isPresent()) {
            CartItem existing = targetItem.get();
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
            cart.getItems().remove(item);
            return;
        }

        item.setVariantId(nextVariantId);
    }



    private void clampCartQuantities(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return;
        }

        cart.getItems().forEach(item -> {
            int quantity = item.getQuantity() == null ? 1 : Math.max(1, item.getQuantity());
            Integer stockQuantity = item.getStockQuantity();
            if (stockQuantity != null && stockQuantity > 0) {
                quantity = Math.min(quantity, stockQuantity);
            }
            item.setQuantity(quantity);
        });
    }

    private List<CartItemVariantResponse> toVariantResponses(List<CartItemVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return List.of();
        }

        return variants.stream()
                .map(this::toVariantResponse)
                .toList();
    }

    private CartItemVariantResponse toVariantResponse(CartItemVariant variant) {
        return CartItemVariantResponse.builder()
                .id(variant.getId())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .length(variant.getLength())
                .width(variant.getWidth())
                .height(variant.getHeight())
                .weight(variant.getWeight())
                .color(variant.getColor())
                .material(variant.getMaterial())
                .warranty(variant.getWarranty())
                .build();
    }

    private CartResponse toResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .total(cart.getTotal())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(cart.getItems() == null ? List.of() : cart.getItems().stream().map(this::toItemResponse).toList())
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .name(item.getName())
                .slug(item.getSlug())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .quantity(item.getQuantity())
                .stockQuantity(item.getStockQuantity())
                .variants(toVariantResponses(item.getVariants()))
                .build();
    }
}

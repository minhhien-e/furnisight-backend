package com.furnisight.cart.service;

import com.furnisight.catalog.ProductSummary;
import com.furnisight.catalog.ProductSummaryVariant;
import com.furnisight.cart.dto.AddToCartRequest;
import com.furnisight.cart.dto.CartItemResponse;
import com.furnisight.cart.dto.CartItemVariantResponse;
import com.furnisight.cart.dto.CartResponse;
import com.furnisight.cart.dto.UpdateCartItemRequest;
import com.furnisight.cart.model.Cart;
import com.furnisight.cart.model.CartItem;
import com.furnisight.cart.model.CartItemVariant;
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
    private final CatalogGrpcClient catalogGrpcClient;

    public CartResponse getCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(enrichCart(cart));
    }

    public CartResponse addToCart(UUID userId, AddToCartRequest request) {
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

        return saveAndRespond(cart);
    }

    public CartResponse updateCartItem(
            UUID userId,
            String productId,
            String variantId,
            UpdateCartItemRequest request
    ) {
        Cart cart = getOrCreateCart(userId);

        findItem(cart, productId, variantId)
                .ifPresent(item -> updateExistingItem(cart, item, request));

        return saveAndRespond(cart);
    }

    public CartResponse removeCartItem(UUID userId, String productId, String variantId) {
        Cart cart = getOrCreateCart(userId);

        cart.getItems().removeIf(item -> isSameItem(item, productId, variantId));

        return saveAndRespond(cart);
    }

    public void clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        saveCart(cart);
    }

    private CartResponse saveAndRespond(Cart cart) {
        Cart savedCart = saveCart(cart);
        Cart enrichedCart = enrichCart(savedCart);
        clampCartQuantities(enrichedCart);
        return toResponse(saveCart(enrichedCart));
    }

    private Cart saveCart(Cart cart) {
        updateCartTotal(cart);
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
                .filter(item -> isSameItem(item, productId, variantId))
                .findFirst();
    }

    private void updateExistingItem(Cart cart, CartItem item, UpdateCartItemRequest request) {
        String nextVariantId = normalizeNullable(request.getVariantId());
        String currentVariantId = normalizeNullable(item.getVariantId());

        item.setQuantity(request.getQuantity());

        if (equalsNullable(currentVariantId, nextVariantId)) {
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

    private boolean isSameItem(CartItem item, String productId, String variantId) {
        return productId.equals(item.getProductId())
                && equalsNullable(variantId, item.getVariantId());
    }

    private boolean equalsNullable(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void updateCartTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        cart.setTotal(total);
        cart.setUpdatedAt(LocalDateTime.now());
    }

    private Cart enrichCart(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return cart;
        }

        List<CatalogGrpcClient.ProductLookupItem> lookupItems = cart.getItems().stream()
                .map(item -> new CatalogGrpcClient.ProductLookupItem(item.getProductId(), item.getVariantId()))
                .filter(item -> item.productId() != null && !item.productId().isBlank())
                .distinct()
                .toList();

        if (lookupItems.isEmpty()) {
            return cart;
        }

        try {
            Map<String, ProductSummary> productMap =
                    catalogGrpcClient.getProductSummaries(lookupItems);

            cart.getItems().forEach(item -> {
                ProductSummary product = productMap.get(lineKey(item));

                if (product != null) {
                    applyProductSummary(item, product);
                }
            });

            updateCartTotal(cart);
        } catch (Exception ex) {
            log.warn(
                    "Failed to enrich cart items from catalog gRPC for cartId={}: {}",
                    cart.getId(),
                    ex.getMessage()
            );
        }

        return cart;
    }

    private void applyProductSummary(CartItem item, ProductSummary product) {
        item.setName(nonBlank(product.getName(), item.getName()));
        item.setImageUrl(nonBlank(product.getImage(), item.getImageUrl()));
        item.setSlug(nonBlank(product.getSlug(), item.getSlug()));
        item.setVariants(toCartVariants(product));

        if (item.getVariants() == null || item.getVariants().isEmpty()) {
            return;
        }

        applyVariant(item, item.getVariants().get(0));
    }

    private String nonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private CartResponse toResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .items(cart.getItems().stream()
                        .map(this::toItemResponse)
                        .toList())
                .total(cart.getTotal())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
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
                .length(item.getLength())
                .width(item.getWidth())
                .height(item.getHeight())
                .weight(item.getWeight())
                .color(item.getColor())
                .material(item.getMaterial())
                .warranty(item.getWarranty())
                .variants(toVariantResponses(item.getVariants()))
                .build();
    }

    private List<CartItemVariant> toCartVariants(ProductSummary product) {
        List<ProductSummaryVariant> variants = product.getVariantsCount() > 0
                ? product.getVariantsList()
                : product.hasVariant() ? List.of(product.getVariant()) : List.of();

        if (variants.isEmpty()) {
            return Collections.emptyList();
        }

        return variants.stream()
                .map(this::toCartVariant)
                .toList();
    }

    private CartItemVariant toCartVariant(ProductSummaryVariant variant) {
        return CartItemVariant.builder()
                .id(nonBlank(variant.getId(), null))
                .price(variant.hasPrice() ? variant.getPrice() : null)
                .stockQuantity(variant.hasStockQuantity() ? variant.getStockQuantity() : null)
                .length(variant.hasLength() ? variant.getLength() : null)
                .width(variant.hasWidth() ? variant.getWidth() : null)
                .height(variant.hasHeight() ? variant.getHeight() : null)
                .weight(variant.hasWeight() ? variant.getWeight() : null)
                .color(nonBlank(variant.getColor(), null))
                .material(nonBlank(variant.getMaterial(), null))
                .warranty(nonBlank(variant.getWarranty(), null))
                .build();
    }

    private void applyVariant(CartItem item, CartItemVariant variant) {
        if (variant.getId() != null && (item.getVariantId() == null || item.getVariantId().isBlank())) {
            item.setVariantId(variant.getId());
        }
        if (variant.getPrice() != null) {
            item.setPrice(variant.getPrice());
        }
        if (variant.getStockQuantity() != null) {
            item.setStockQuantity(variant.getStockQuantity());
        }
        if (variant.getLength() != null) {
            item.setLength(variant.getLength());
        }
        if (variant.getWidth() != null) {
            item.setWidth(variant.getWidth());
        }
        if (variant.getHeight() != null) {
            item.setHeight(variant.getHeight());
        }
        if (variant.getWeight() != null) {
            item.setWeight(variant.getWeight());
        }

        item.setColor(nonBlank(variant.getColor(), item.getColor()));
        item.setMaterial(nonBlank(variant.getMaterial(), item.getMaterial()));
        item.setWarranty(nonBlank(variant.getWarranty(), item.getWarranty()));
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

    private String lineKey(CartItem item) {
        return CatalogGrpcClient.keyOf(item.getProductId(), item.getVariantId());
    }
}

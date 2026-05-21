package com.furnisight.cart.service;

import com.furnisight.catalog.ProductSummary;
import com.furnisight.catalog.ProductSummaryVariant;
import com.furnisight.cart.dto.AddToCartRequest;
import com.furnisight.cart.dto.CartItemResponse;
import com.furnisight.cart.dto.CartResponse;
import com.furnisight.cart.dto.UpdateCartItemRequest;
import com.furnisight.cart.model.Cart;
import com.furnisight.cart.model.CartItem;
import com.furnisight.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
                .ifPresent(item -> item.setQuantity(request.getQuantity()));

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
        return toResponse(enrichCart(savedCart));
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

    private boolean isSameItem(CartItem item, String productId, String variantId) {
        return productId.equals(item.getProductId())
                && equalsNullable(variantId, item.getVariantId());
    }

    private boolean equalsNullable(String a, String b) {
        return a == null ? b == null : a.equals(b);
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

        List<String> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));

        if (productIds.isEmpty()) {
            return cart;
        }

        try {
            Map<String, ProductSummary> productMap =
                    catalogGrpcClient.getProductSummaries(productIds);

            cart.getItems().forEach(item -> {
                ProductSummary product = productMap.get(item.getProductId());

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

        if (!product.hasVariant()) {
            return;
        }

        ProductSummaryVariant variant = product.getVariant();

        if (!canApplyVariant(item, variant)) {
            return;
        }

        if (variant.hasPrice()) {
            item.setPrice(variant.getPrice());
        }

        if (variant.hasOldPrice()) {
            item.setOldPrice(variant.getOldPrice());
        }

        if (variant.hasStockQuantity()) {
            item.setStockQuantity(variant.getStockQuantity());
        }

        if (variant.hasLength()) {
            item.setLength(variant.getLength());
        }

        if (variant.hasWidth()) {
            item.setWidth(variant.getWidth());
        }

        if (variant.hasHeight()) {
            item.setHeight(variant.getHeight());
        }

        if (variant.hasWeight()) {
            item.setWeight(variant.getWeight());
        }

        item.setColor(nonBlank(variant.getColor(), item.getColor()));
    }

    private boolean canApplyVariant(CartItem item, ProductSummaryVariant variant) {
        String itemVariantId = item.getVariantId();

        return itemVariantId == null
                || itemVariantId.isBlank()
                || itemVariantId.equals(variant.getId());
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
                .oldPrice(item.getOldPrice())
                .imageUrl(item.getImageUrl())
                .quantity(item.getQuantity())
                .stockQuantity(item.getStockQuantity())
                .length(item.getLength())
                .width(item.getWidth())
                .height(item.getHeight())
                .weight(item.getWeight())
                .color(item.getColor())
                .build();
    }
}
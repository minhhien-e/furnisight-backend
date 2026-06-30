package com.furnisight.cart.utils;

import com.furnisight.cart.model.CartItem;

public final class CartUtils {

    private CartUtils() {
    }

    public static boolean isSameItem(CartItem item, String productId, String variantId) {
        return productId.equals(item.getProductId())
                && equalsNullable(variantId, item.getVariantId());
    }

    public static boolean equalsNullable(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public static String nonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    public static void updateCartTotal(com.furnisight.cart.model.Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * (item.getQuantity() != null ? item.getQuantity() : 1))
                .sum();

        cart.setTotal(total);
        cart.setUpdatedAt(java.time.LocalDateTime.now());
    }
}

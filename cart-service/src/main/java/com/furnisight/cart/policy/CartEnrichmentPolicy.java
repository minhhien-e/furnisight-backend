package com.furnisight.cart.policy;

import com.furnisight.cart.model.Cart;
import com.furnisight.cart.model.CartItem;
import com.furnisight.cart.model.CartItemVariant;
import com.furnisight.cart.service.CatalogGrpcClient;
import com.furnisight.cart.utils.CartUtils;
import com.furnisight.catalog.ProductSummary;
import com.furnisight.catalog.ProductSummaryVariant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartEnrichmentPolicy {

    private final CatalogGrpcClient catalogGrpcClient;

    public Cart enrichCart(Cart cart, String locale) {
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
            Map<String, ProductSummary> productMap = catalogGrpcClient.getProductSummaries(lookupItems, locale);

            cart.getItems().forEach(item -> {
                ProductSummary product = productMap.get(CatalogGrpcClient.keyOf(item.getProductId(), item.getVariantId()));
                if (product != null) {
                    applyProductSummary(item, product);
                }
            });

            CartUtils.updateCartTotal(cart);
        } catch (Exception ex) {
            log.warn("Failed to enrich cart items from catalog gRPC for cartId={}: {}", cart.getId(), ex.getMessage());
        }

        return cart;
    }

    private void applyProductSummary(CartItem item, ProductSummary product) {
        item.setName(CartUtils.nonBlank(product.getName(), item.getName()));
        item.setImageUrl(CartUtils.nonBlank(product.getImage(), item.getImageUrl()));
        item.setSlug(CartUtils.nonBlank(product.getSlug(), item.getSlug()));
        item.setVariants(toCartVariants(product));

        if (item.getVariants() == null || item.getVariants().isEmpty()) {
            return;
        }

        applyVariant(item, item.getVariants().get(0));
    }

    private List<CartItemVariant> toCartVariants(ProductSummary product) {
        List<ProductSummaryVariant> variants = product.getVariantsCount() > 0
                ? product.getVariantsList()
                : product.hasVariant() ? List.of(product.getVariant()) : List.of();

        if (variants.isEmpty()) {
            return Collections.emptyList();
        }

        return variants.stream().map(this::toCartVariant).toList();
    }

    private CartItemVariant toCartVariant(ProductSummaryVariant variant) {
        return CartItemVariant.builder()
                .id(CartUtils.nonBlank(variant.getId(), null))
                .price(variant.hasPrice() ? variant.getPrice() : null)
                .stockQuantity(variant.hasStockQuantity() ? variant.getStockQuantity() : null)
                .length(variant.hasLength() ? variant.getLength() : null)
                .width(variant.hasWidth() ? variant.getWidth() : null)
                .height(variant.hasHeight() ? variant.getHeight() : null)
                .weight(variant.hasWeight() ? variant.getWeight() : null)
                .color(CartUtils.nonBlank(variant.getColor(), null))
                .material(CartUtils.nonBlank(variant.getMaterial(), null))
                .warranty(CartUtils.nonBlank(variant.getWarranty(), null))
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

        item.setColor(CartUtils.nonBlank(variant.getColor(), item.getColor()));
        item.setMaterial(CartUtils.nonBlank(variant.getMaterial(), item.getMaterial()));
        item.setWarranty(CartUtils.nonBlank(variant.getWarranty(), item.getWarranty()));
    }
}

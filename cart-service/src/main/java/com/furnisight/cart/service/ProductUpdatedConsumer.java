package com.furnisight.cart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.cart.dto.event.ProductUpdatedEvent;
import com.furnisight.cart.model.Cart;
import com.furnisight.cart.model.CartItem;
import com.furnisight.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductUpdatedConsumer {
    private static final String TOPIC = "product-updated";

    private final CartRepository cartRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        ProductUpdatedEvent event = objectMapper.readValue(payload, ProductUpdatedEvent.class);
        if (event.productId() == null || event.productId().isBlank()) {
            return;
        }

        List<Cart> carts = cartRepository.findByItemsProductId(event.productId());
        for (Cart cart : carts) {
            boolean changed = false;
            for (CartItem item : cart.getItems()) {
                if (!event.productId().equals(item.getProductId())) {
                    continue;
                }
                changed = applyProductUpdate(item, event) || changed;
            }
            if (changed) {
                refreshTotal(cart);
                cartRepository.save(cart);
            }
        }

        log.info("Applied product-updated event for productId={} to {} carts", event.productId(), carts.size());
    }

    private boolean applyProductUpdate(CartItem item, ProductUpdatedEvent event) {
        boolean changed = false;
        if (event.name() != null && !event.name().isBlank() && !event.name().equals(item.getName())) {
            item.setName(event.name());
            changed = true;
        }
        if (event.imageUrl() != null && !event.imageUrl().isBlank() && !event.imageUrl().equals(item.getImageUrl())) {
            item.setImageUrl(event.imageUrl());
            changed = true;
        }

        ProductUpdatedEvent.ProductUpdatedVariantEvent variant = selectedVariant(item, event);
        if (variant != null && variant.price() != null && !variant.price().equals(item.getPrice())) {
            item.setPrice(variant.price());
            changed = true;
        }
        return changed;
    }

    private ProductUpdatedEvent.ProductUpdatedVariantEvent selectedVariant(CartItem item, ProductUpdatedEvent event) {
        if (event.variants() == null || event.variants().isEmpty()) {
            return null;
        }
        if (item.getVariantId() == null || item.getVariantId().isBlank()) {
            return event.variants().get(0);
        }
        return event.variants().stream()
                .filter(variant -> item.getVariantId().equals(variant.id()))
                .findFirst()
                .orElse(null);
    }

    private void refreshTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> valueOrZero(item.getPrice()) * valueOrZero(item.getQuantity()))
                .sum();
        cart.setTotal(total);
        cart.setUpdatedAt(LocalDateTime.now());
    }

    private double valueOrZero(Number value) {
        return value == null ? 0.0 : value.doubleValue();
    }
}

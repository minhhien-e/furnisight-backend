package com.furnisight.order.adapter.in.web.rest;

import com.furnisight.order.application.order.port.in.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class OrderTranslationService {

    private static final String SOURCE_LANG = "vi";
    private static final String TARGET_LANG = "en";

    private final boolean enabled;
    private final RestClient restClient;
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> inFlight = new ConcurrentHashMap<>();

    public OrderTranslationService(
            RestClient.Builder restClientBuilder,
            @Value("${translation.enabled:true}") boolean enabled,
            @Value("${translation.base-url:http://libretranslate:5000}") String baseUrl
    ) {
        this.enabled = enabled;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return SOURCE_LANG;
        }
        String normalized = lang.trim().toLowerCase();
        return normalized.startsWith(TARGET_LANG) ? TARGET_LANG : SOURCE_LANG;
    }

    public List<OrderResponse> localizeOrders(List<OrderResponse> orders, String lang) {
        if (orders == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return orders;
        }
        orders.forEach(order -> localizeOrder(order, lang));
        return orders;
    }

    public OrderResponse localizeOrder(OrderResponse order, String lang) {
        if (order == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return order;
        }

        order.setCustomerNote(translateValue(order.getCustomerNote()));

        if (order.getItems() != null) {
            order.getItems().forEach(item -> {
                OrderResponse.ProductSnapshotResponse snapshot = item.getProductSnapshot();
                if (snapshot != null) {
                    snapshot.setCategoryName(translateValue(snapshot.getCategoryName()));
                    snapshot.setProductName(translateValue(snapshot.getProductName()));
                    snapshot.setColor(translateValue(snapshot.getColor()));
                    snapshot.setMaterial(translateValue(snapshot.getMaterial()));
                    snapshot.setWarranty(translateValue(snapshot.getWarranty()));
                }
            });
        }

        if (order.getStatusHistory() != null) {
            order.getStatusHistory().forEach(history -> history.setNote(translateValue(history.getNote())));
        }

        return order;
    }

    private String translateValue(String value) {
        if (!enabled || value == null || value.isBlank()) {
            return value;
        }
        String cacheKey = buildCacheKey(value);
        String cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        CompletableFuture<String> future = inFlight.computeIfAbsent(cacheKey, key ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        TranslateResponse response = restClient.post()
                                .uri("/translate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(new TranslateRequest(value, SOURCE_LANG, TARGET_LANG))
                                .retrieve()
                                .body(TranslateResponse.class);
                        String translated = response == null || response.translatedText() == null
                                || response.translatedText().isBlank()
                                ? value
                                : response.translatedText();
                        cache.put(key, translated);
                        return translated;
                    } catch (Exception ex) {
                        log.warn("Order translation failed for text='{}': {}", value, ex.getMessage());
                        return value;
                    }
                }));

        try {
            return future.join();
        } finally {
            inFlight.remove(cacheKey, future);
        }
    }

    private String buildCacheKey(String value) {
        return SOURCE_LANG + ":" + TARGET_LANG + ":" + value.trim().replaceAll("\\s+", " ");
    }

    private record TranslateRequest(String q, String source, String target) {
    }

    private record TranslateResponse(String translatedText) {
    }
}

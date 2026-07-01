package com.furnisight.promotion.adapter.in.web.rest;

import com.furnisight.promotion.application.dto.MarketingComboDto;
import com.furnisight.promotion.domain.common.PageResponse;
import com.furnisight.promotion.application.dto.PromotionDto;
import com.furnisight.promotion.application.dto.RecommendVouchersResponse;
import com.furnisight.promotion.application.dto.ValidateComboResponse;
import com.furnisight.promotion.application.dto.ValidateVoucherResponse;
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
public class PromotionTranslationService {

    private static final String SOURCE_LANG = "vi";
    private static final String TARGET_LANG = "en";

    private final boolean enabled;
    private final RestClient restClient;
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> inFlight = new ConcurrentHashMap<>();

    public PromotionTranslationService(
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

    public List<PromotionDto> localizePromotions(List<PromotionDto> promotions, String lang) {
        if (promotions == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return promotions;
        }
        promotions.forEach(this::localizePromotion);
        return promotions;
    }

    public PageResponse<PromotionDto> localizePromotionPage(PageResponse<PromotionDto> page, String lang) {
        if (page == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return page;
        }
        return new PageResponse<>(
                localizePromotions(page.items(), lang),
                page.totalPages(),
                page.totalElements(),
                page.currentPage(),
                page.pageSize()
        );
    }

    public RecommendVouchersResponse localizeRecommendResponse(RecommendVouchersResponse response, String lang) {
        if (response == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return response;
        }
        localizePromotion(response.getShopVoucher());
        localizePromotion(response.getShippingVoucher());
        return response;
    }

    public ValidateVoucherResponse localizeValidateVoucherResponse(ValidateVoucherResponse response, String lang) {
        if (response == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return response;
        }
        response.setMessage(translateValue(response.getMessage()));
        localizePromotion(response.getVoucher());
        return response;
    }

    public List<MarketingComboDto> localizeCombos(List<MarketingComboDto> combos, String lang) {
        if (combos == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return combos;
        }
        combos.forEach(this::localizeCombo);
        return combos;
    }

    public PageResponse<MarketingComboDto> localizeComboPage(PageResponse<MarketingComboDto> page, String lang) {
        if (page == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return page;
        }
        return new PageResponse<>(
                localizeCombos(page.items(), lang),
                page.totalPages(),
                page.totalElements(),
                page.currentPage(),
                page.pageSize()
        );
    }

    public ValidateComboResponse localizeValidateComboResponse(ValidateComboResponse response, String lang) {
        if (response == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return response;
        }
        response.setComboName(translateValue(response.getComboName()));
        response.setMessage(translateValue(response.getMessage()));
        return response;
    }

    private void localizePromotion(PromotionDto promotion) {
        if (promotion == null) {
            return;
        }
        promotion.setName(translateValue(promotion.getName()));
        promotion.setDescription(translateValue(promotion.getDescription()));
        promotion.setStatusLabel(translateValue(promotion.getStatusLabel()));
    }

    private void localizeCombo(MarketingComboDto combo) {
        if (combo == null) {
            return;
        }
        combo.setName(translateValue(combo.getName()));
        combo.setDescription(translateValue(combo.getDescription()));
        if (combo.getItems() != null) {
            combo.getItems().forEach(item -> {
                item.setProductName(translateValue(item.getProductName()));
                item.setCategoryName(translateValue(item.getCategoryName()));
            });
        }
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
                        log.warn("Promotion translation failed for text='{}': {}", value, ex.getMessage());
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

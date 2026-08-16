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
            @Value("${translation.enabled:true}") boolean enabled,
            @Value("${translation.base-url:http://libretranslate:5000}") String baseUrl,
            @Value("${translation.timeout:20s}") java.time.Duration timeout
    ) {
        this.enabled = enabled;
        
        org.springframework.http.client.JdkClientHttpRequestFactory requestFactory = new org.springframework.http.client.JdkClientHttpRequestFactory(
                java.net.http.HttpClient.newBuilder()
                        .connectTimeout(timeout)
                        .build());
        requestFactory.setReadTimeout(timeout);
        
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return SOURCE_LANG;
        }
        String normalized = lang.trim().toLowerCase();
        return normalized.startsWith(TARGET_LANG) ? TARGET_LANG : SOURCE_LANG;
    }

    private class TranslationBatcher {
        private final List<String> texts = new java.util.ArrayList<>();
        private final List<java.util.function.Consumer<String>> setters = new java.util.ArrayList<>();

        public void add(String text, java.util.function.Consumer<String> setter) {
            if (text != null && !text.isBlank()) {
                texts.add(text);
                setters.add(setter);
            }
        }

        public void execute(String source, String target) {
            if (texts.isEmpty() || !enabled) return;
            
            // Collect unique texts to translate to avoid translating same string multiple times
            List<String> uniqueTexts = texts.stream().distinct().toList();
            Map<String, String> translationMap = new java.util.HashMap<>();
            
            // Check cache
            List<String> toTranslate = new java.util.ArrayList<>();
            for (String text : uniqueTexts) {
                String cacheKey = buildCacheKey(text);
                if (cache.containsKey(cacheKey)) {
                    translationMap.put(text, cache.get(cacheKey));
                } else {
                    toTranslate.add(text);
                }
            }

            if (!toTranslate.isEmpty()) {
                try {
                    TranslateBatchResponse response = restClient.post()
                            .uri("/translate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new TranslateBatchRequest(toTranslate, source, target))
                            .retrieve()
                            .body(TranslateBatchResponse.class);
                    
                    if (response != null && response.translatedText() != null && response.translatedText().size() == toTranslate.size()) {
                        for (int i = 0; i < toTranslate.size(); i++) {
                            String orig = toTranslate.get(i);
                            String trans = response.translatedText().get(i);
                            translationMap.put(orig, trans);
                            cache.put(buildCacheKey(orig), trans);
                        }
                    } else {
                        // Fallback to original
                        for (String text : toTranslate) {
                            translationMap.put(text, text);
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Batch promotion translation failed for {} items: {}", toTranslate.size(), ex.getMessage());
                    for (String text : toTranslate) {
                        translationMap.put(text, text);
                    }
                }
            }

            // Apply translations
            for (int i = 0; i < texts.size(); i++) {
                String orig = texts.get(i);
                String trans = translationMap.getOrDefault(orig, orig);
                setters.get(i).accept(trans);
            }
        }
    }

    public List<PromotionDto> localizePromotions(List<PromotionDto> promotions, String lang) {
        if (promotions == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return promotions;
        }
        TranslationBatcher batcher = new TranslationBatcher();
        promotions.forEach(p -> collectPromotion(p, batcher));
        batcher.execute(SOURCE_LANG, TARGET_LANG);
        return promotions;
    }

    public PageResponse<PromotionDto> localizePromotionPage(PageResponse<PromotionDto> page, String lang) {
        if (page == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return page;
        }
        return new PageResponse<>(
                localizePromotions(page.getItems(), lang),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getCurrentPage(),
                page.getPageSize()
        );
    }

    public RecommendVouchersResponse localizeRecommendResponse(RecommendVouchersResponse response, String lang) {
        if (response == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return response;
        }
        TranslationBatcher batcher = new TranslationBatcher();
        collectPromotion(response.getShopVoucher(), batcher);
        collectPromotion(response.getShippingVoucher(), batcher);
        batcher.execute(SOURCE_LANG, TARGET_LANG);
        return response;
    }

    public ValidateVoucherResponse localizeValidateVoucherResponse(ValidateVoucherResponse response, String lang) {
        if (response == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return response;
        }
        TranslationBatcher batcher = new TranslationBatcher();
        batcher.add(response.getMessage(), response::setMessage);
        collectPromotion(response.getVoucher(), batcher);
        batcher.execute(SOURCE_LANG, TARGET_LANG);
        return response;
    }

    public List<MarketingComboDto> localizeCombos(List<MarketingComboDto> combos, String lang) {
        if (combos == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return combos;
        }
        TranslationBatcher batcher = new TranslationBatcher();
        combos.forEach(c -> collectCombo(c, batcher));
        batcher.execute(SOURCE_LANG, TARGET_LANG);
        return combos;
    }

    public PageResponse<MarketingComboDto> localizeComboPage(PageResponse<MarketingComboDto> page, String lang) {
        if (page == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return page;
        }
        return new PageResponse<>(
                localizeCombos(page.getItems(), lang),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getCurrentPage(),
                page.getPageSize()
        );
    }

    public ValidateComboResponse localizeValidateComboResponse(ValidateComboResponse response, String lang) {
        if (response == null || !TARGET_LANG.equals(normalizeLang(lang))) {
            return response;
        }
        TranslationBatcher batcher = new TranslationBatcher();
        batcher.add(response.getComboName(), response::setComboName);
        batcher.add(response.getMessage(), response::setMessage);
        batcher.execute(SOURCE_LANG, TARGET_LANG);
        return response;
    }

    private void collectPromotion(PromotionDto promotion, TranslationBatcher batcher) {
        if (promotion == null) {
            return;
        }
        batcher.add(promotion.getName(), promotion::setName);
        batcher.add(promotion.getDescription(), promotion::setDescription);
        batcher.add(promotion.getStatusLabel(), promotion::setStatusLabel);
    }

    private void collectCombo(MarketingComboDto combo, TranslationBatcher batcher) {
        if (combo == null) {
            return;
        }
        batcher.add(combo.getName(), combo::setName);
        batcher.add(combo.getDescription(), combo::setDescription);
        if (combo.getItems() != null) {
            combo.getItems().forEach(item -> {
                batcher.add(item.getProductName(), item::setProductName);
                batcher.add(item.getCategoryName(), item::setCategoryName);
            });
        }
    }

    private String buildCacheKey(String value) {
        return SOURCE_LANG + ":" + TARGET_LANG + ":" + value.trim().replaceAll("\\s+", " ");
    }

    private record TranslateBatchRequest(List<String> q, String source, String target) {
    }

    private record TranslateBatchResponse(List<String> translatedText) {
    }
}

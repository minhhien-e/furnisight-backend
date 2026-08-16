package com.furnisight.catalog.infrastructure.translation;

import com.furnisight.catalog.application.translation.port.out.TextTranslationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CachedTextTranslationAdapter implements TextTranslationPort {

    private final TranslationProperties properties;
    private final TranslationCacheStore translationCacheStore;
    private final LibreTranslateHttpClient libreTranslateHttpClient;
    private final Map<String, CompletableFuture<String>> inFlight = new ConcurrentHashMap<>();

    @Override
    public String translate(String text, String sourceLang, String targetLang) {
        if (!properties.isEnabled() || text == null || text.isBlank()) {
            return text;
        }

        String normalizedSource = normalizeLanguage(sourceLang);
        String normalizedTarget = normalizeLanguage(targetLang);
        if (normalizedSource.equals(normalizedTarget)) {
            return text;
        }

        String cacheKey = buildCacheKey(normalizedSource, normalizedTarget, text);
        return translationCacheStore.get(cacheKey).orElseGet(() -> translateAndCache(cacheKey, text, normalizedSource, normalizedTarget));
    }

    private String translateAndCache(String cacheKey, String originalText, String sourceLang, String targetLang) {
        boolean[] isNew = {false};
        CompletableFuture<String> future = inFlight.computeIfAbsent(cacheKey, key -> {
            isNew[0] = true;
            return new CompletableFuture<>();
        });

        if (isNew[0]) {
            try {
                String translated = libreTranslateHttpClient.translate(originalText, sourceLang, targetLang);
                translationCacheStore.put(cacheKey, translated);
                future.complete(translated);
            } catch (Exception ex) {
                log.warn("Translation failed for {} -> {} text='{}'", sourceLang, targetLang, originalText, ex);
                future.complete(originalText);
            } finally {
                inFlight.remove(cacheKey, future);
            }
        }

        return future.join();
    }

    @Override
    public java.util.List<String> translateBatch(java.util.List<String> texts, String sourceLang, String targetLang) {
        if (!properties.isEnabled() || texts == null || texts.isEmpty()) {
            return texts;
        }

        String normalizedSource = normalizeLanguage(sourceLang);
        String normalizedTarget = normalizeLanguage(targetLang);
        if (normalizedSource.equals(normalizedTarget)) {
            return texts;
        }

        java.util.List<String> results = new java.util.ArrayList<>(texts.size());
        java.util.List<Integer> uncachedIndices = new java.util.ArrayList<>();
        java.util.List<String> uncachedTexts = new java.util.ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (text == null || text.isBlank()) {
                results.add(text);
                continue;
            }
            String cacheKey = buildCacheKey(normalizedSource, normalizedTarget, text);
            java.util.Optional<String> cached = translationCacheStore.get(cacheKey);
            if (cached.isPresent()) {
                results.add(cached.get());
            } else {
                results.add(null);
                uncachedIndices.add(i);
                uncachedTexts.add(text);
            }
        }

        if (!uncachedTexts.isEmpty()) {
            // Deduplicate uncached texts to avoid translating the same string multiple times
            java.util.List<String> uniqueUncached = uncachedTexts.stream().distinct().toList();
            
            // Translate all texts synchronously in batches
            java.util.List<String> translatedUnique = translateAndCacheBatch(uniqueUncached, normalizedSource, normalizedTarget);
            
            java.util.Map<String, String> translationMap = new java.util.HashMap<>();
            for (int i = 0; i < uniqueUncached.size(); i++) {
                translationMap.put(uniqueUncached.get(i), translatedUnique.get(i));
            }
            
            for (int i = 0; i < uncachedTexts.size(); i++) {
                results.set(uncachedIndices.get(i), translationMap.get(uncachedTexts.get(i)));
            }
        }

        return results;
    }

    private java.util.List<String> translateAndCacheBatch(java.util.List<String> originalTexts, String sourceLang, String targetLang) {
        int chunkSize = 8;
        java.util.List<CompletableFuture<java.util.List<String>>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < originalTexts.size(); i += chunkSize) {
            int end = Math.min(originalTexts.size(), i + chunkSize);
            java.util.List<String> chunk = originalTexts.subList(i, end);

            CompletableFuture<java.util.List<String>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return libreTranslateHttpClient.translateBatch(chunk, sourceLang, targetLang);
                } catch (Exception ex) {
                    log.warn("Batch chunk translation failed for {} -> {} with {} items", sourceLang, targetLang, chunk.size(), ex);
                    return null; // Return null to indicate failure for this chunk
                }
            });
            futures.add(future);
        }

        java.util.List<String> translated = new java.util.ArrayList<>(originalTexts.size());
        for (int i = 0; i < futures.size(); i++) {
            java.util.List<String> chunkResult = futures.get(i).join();
            int startIdx = i * chunkSize;
            int endIdx = Math.min(originalTexts.size(), startIdx + chunkSize);
            java.util.List<String> originalChunk = originalTexts.subList(startIdx, endIdx);
            
            if (chunkResult == null || chunkResult.size() != originalChunk.size()) {
                // If chunk failed, fallback to original texts and do NOT cache
                translated.addAll(originalChunk);
            } else {
                // Chunk succeeded, add to results and cache them
                translated.addAll(chunkResult);
                for (int j = 0; j < chunkResult.size(); j++) {
                    String orig = originalChunk.get(j);
                    String trans = chunkResult.get(j);
                    // Prevent cache poisoning if LibreTranslate fails silently and returns original text
                    if (trans != null && !trans.equals(orig)) {
                        String cacheKey = buildCacheKey(sourceLang, targetLang, orig);
                        translationCacheStore.put(cacheKey, trans);
                    }
                }
            }
        }

        return translated;
    }

    private String buildCacheKey(String sourceLang, String targetLang, String text) {
        return "translation:%s:%s:%s".formatted(sourceLang, targetLang, normalizeText(text));
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s+", " ");
    }

    private String normalizeLanguage(String lang) {
        if (lang == null || lang.isBlank()) {
            return "vi";
        }
        String normalized = lang.trim().toLowerCase();
        return normalized.startsWith("en") ? "en" : "vi";
    }
}

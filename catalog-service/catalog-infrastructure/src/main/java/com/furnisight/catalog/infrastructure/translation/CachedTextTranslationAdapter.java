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

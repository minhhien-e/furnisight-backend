package com.furnisight.catalog.infrastructure.translation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Slf4j
@Component
public class LibreTranslateHttpClient {

    private final TranslationProperties properties;
    private final RestClient restClient;

    public LibreTranslateHttpClient(TranslationProperties properties) {
        this.properties = properties;

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(properties.getTimeout())
                        .build());
        requestFactory.setReadTimeout(properties.getTimeout());

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public String translate(String text, String sourceLang, String targetLang) {
        LibreTranslateResponse response = restClient.post()
                .uri("/translate")
                .body(new LibreTranslateRequest(text, sourceLang, targetLang))
                .retrieve()
                .body(LibreTranslateResponse.class);

        if (response == null || response.translatedText() == null || response.translatedText().isBlank()) {
            throw new IllegalStateException("LibreTranslate returned empty translatedText");
        }

        return response.translatedText().trim();
    }

    public java.util.List<String> translateBatch(java.util.List<String> texts, String sourceLang, String targetLang) {
        if (texts == null || texts.isEmpty()) {
            return texts;
        }

        LibreTranslateBatchResponse response = restClient.post()
                .uri("/translate")
                .body(new LibreTranslateBatchRequest(texts, sourceLang, targetLang))
                .retrieve()
                .body(LibreTranslateBatchResponse.class);

        if (response == null || response.translatedText() == null || response.translatedText().isEmpty()) {
            throw new IllegalStateException("LibreTranslate returned empty translatedText for batch");
        }

        return response.translatedText();
    }

    private record LibreTranslateRequest(String q, String source, String target) {
    }

    private record LibreTranslateResponse(String translatedText) {
    }

    private record LibreTranslateBatchRequest(java.util.List<String> q, String source, String target) {
    }

    private record LibreTranslateBatchResponse(java.util.List<String> translatedText) {
    }
}

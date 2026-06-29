package com.furnisight.catalog.infrastructure.translation;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CachedTextTranslationAdapterTest {

    @Test
    void cacheHitAvoidsSecondOutboundTranslation() {
        TranslationProperties properties = new TranslationProperties();
        properties.setCacheTtl(Duration.ofMinutes(10));
        properties.setCacheMaximumSize(100);
        TranslationCacheStore cacheStore = new TranslationCacheStore(properties, (org.springframework.data.redis.core.StringRedisTemplate) null);
        AtomicInteger calls = new AtomicInteger();

        LibreTranslateHttpClient client = new StubLibreTranslateHttpClient(properties, text -> {
            calls.incrementAndGet();
            return "Sofa";
        });

        CachedTextTranslationAdapter adapter = new CachedTextTranslationAdapter(properties, cacheStore, client);

        assertThat(adapter.translate("Ghế sofa", "vi", "en")).isEqualTo("Sofa");
        assertThat(adapter.translate("Ghế sofa", "vi", "en")).isEqualTo("Sofa");
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void concurrentMissCoalescesToSingleOutboundCall() throws Exception {
        TranslationProperties properties = new TranslationProperties();
        properties.setCacheTtl(Duration.ofMinutes(10));
        properties.setCacheMaximumSize(100);
        TranslationCacheStore cacheStore = new TranslationCacheStore(properties, (org.springframework.data.redis.core.StringRedisTemplate) null);
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);

        LibreTranslateHttpClient client = new StubLibreTranslateHttpClient(properties, text -> {
            start.await(1, TimeUnit.SECONDS);
            calls.incrementAndGet();
            return "Sofa";
        });

        CachedTextTranslationAdapter adapter = new CachedTextTranslationAdapter(properties, cacheStore, client);
        var executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(() -> adapter.translate("Ghế sofa", "vi", "en"));
            Future<String> second = executor.submit(() -> adapter.translate("Ghế sofa", "vi", "en"));
            start.countDown();

            assertThat(first.get(2, TimeUnit.SECONDS)).isEqualTo("Sofa");
            assertThat(second.get(2, TimeUnit.SECONDS)).isEqualTo("Sofa");
            assertThat(calls.get()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private static final class StubLibreTranslateHttpClient extends LibreTranslateHttpClient {
        private final ThrowingTranslator translator;

        private StubLibreTranslateHttpClient(TranslationProperties properties, ThrowingTranslator translator) {
            super(properties);
            this.translator = translator;
        }

        @Override
        public String translate(String text, String sourceLang, String targetLang) {
            try {
                return translator.translate(text);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingTranslator {
        String translate(String text) throws Exception;
    }
}

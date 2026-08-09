package com.furnisight.catalog.infrastructure.translation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "translation")
public class TranslationProperties {
    private boolean enabled = true;
    private String provider = "libretranslate";
    private String baseUrl = "http://libretranslate:5000";
    private Duration timeout = Duration.ofSeconds(3);
    private Duration cacheTtl = Duration.ofDays(30);
    private long cacheMaximumSize = 10_000;
}

package com.furnisight.gateway.security;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
public class RateLimitConfig {
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {

            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return Mono.just("OPTIONS_" + System.nanoTime());
            }

            String clientIp = firstHeaderValue(exchange.getRequest().getHeaders().getFirst("CF-Connecting-IP"));
            if (clientIp == null) {
                clientIp = firstHeaderValue(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"));
            }
            if (clientIp != null) {
                return Mono.just(clientIp);
            }

            var address = exchange.getRequest().getRemoteAddress();
            if (address == null) {
                return Mono.just("UNKNOWN");
            }

            return Mono.just(address.getHostString());
        };
    }

    private String firstHeaderValue(String value) {
        return Optional.ofNullable(value)
                .map(header -> header.split(",", 2)[0].trim())
                .filter(header -> !header.isEmpty())
                .orElse(null);
    }
}

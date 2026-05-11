package com.furnisight.gateway.security;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;


@Configuration
public class RateLimitConfig {
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {

            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return Mono.just("OPTIONS_" + System.nanoTime());
            }

            var address = exchange.getRequest().getRemoteAddress();
            if (address == null) {
                return Mono.just("UNKNOWN");
            }

            return Mono.just(address.getHostString());
        };
    }
}

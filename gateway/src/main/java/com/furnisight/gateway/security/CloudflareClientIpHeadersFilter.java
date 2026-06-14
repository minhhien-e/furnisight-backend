package com.furnisight.gateway.security;

import java.util.Optional;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class CloudflareClientIpHeadersFilter implements GlobalFilter, HttpHeadersFilter, Ordered {

    static final String CF_CONNECTING_IP = "CF-Connecting-IP";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = sanitize(exchange.getRequest().getHeaders());
        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(mutatedHeaders -> {
                    mutatedHeaders.clear();
                    mutatedHeaders.addAll(headers);
                })
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public HttpHeaders filter(HttpHeaders input, ServerWebExchange exchange) {
        return sanitize(input);
    }

    private HttpHeaders sanitize(HttpHeaders input) {
        HttpHeaders output = new HttpHeaders();
        output.putAll(input);

        String clientIp = firstHeaderValue(input.getFirst(CF_CONNECTING_IP));
        if (clientIp == null) {
            clientIp = firstHeaderValue(input.getFirst("X-Forwarded-For"));
        }

        output.remove("X-Forwarded-For");
        String normalizedClientIp = normalizeForForwardedHeader(clientIp);
        if (normalizedClientIp != null) {
            output.set("X-Forwarded-For", normalizedClientIp);
        }

        return output;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    static String normalizeForForwardedHeader(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String candidate = value.trim();
        if (candidate.indexOf('\r') >= 0 || candidate.indexOf('\n') >= 0) {
            return null;
        }

        if (candidate.startsWith("[")) {
            int closingBracket = candidate.indexOf(']');
            return closingBracket > 1 ? candidate.substring(0, closingBracket + 1) : null;
        }

        long colonCount = candidate.chars().filter(character -> character == ':').count();
        if (colonCount > 1) {
            return "[" + candidate + "]";
        }

        return candidate;
    }

    private static String firstHeaderValue(String value) {
        return Optional.ofNullable(value)
                .map(header -> header.split(",", 2)[0].trim())
                .filter(header -> !header.isEmpty())
                .orElse(null);
    }
}

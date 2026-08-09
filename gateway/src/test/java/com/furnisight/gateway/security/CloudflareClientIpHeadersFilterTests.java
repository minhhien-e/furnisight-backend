package com.furnisight.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class CloudflareClientIpHeadersFilterTests {

    private final CloudflareClientIpHeadersFilter filter = new CloudflareClientIpHeadersFilter();

    @Test
    void prefersCloudflareClientIpAndReplacesExistingProxyChain() {
        HttpHeaders input = new HttpHeaders();
        input.set(CloudflareClientIpHeadersFilter.CF_CONNECTING_IP, "2001:4860:4860::8888");
        input.set("X-Forwarded-For", "10.0.0.1, 172.18.0.1");
        HttpHeaders headers = filter(input);

        assertThat(headers.getFirst("X-Forwarded-For"))
                .isEqualTo("[2001:4860:4860::8888]");
    }

    @Test
    void keepsIpv4WithoutBrackets() {
        HttpHeaders input = new HttpHeaders();
        input.set(CloudflareClientIpHeadersFilter.CF_CONNECTING_IP, "113.161.72.1");
        HttpHeaders headers = filter(input);

        assertThat(headers.getFirst("X-Forwarded-For")).isEqualTo("113.161.72.1");
    }

    @Test
    void normalizesAlreadyBracketedIpv6AndRemovesPort() {
        assertThat(CloudflareClientIpHeadersFilter.normalizeForForwardedHeader(
                "[2001:4860:4860::8888]:443"))
                .isEqualTo("[2001:4860:4860::8888]");
    }

    @Test
    void fallsBackToFirstForwardedIpWhenCloudflareHeaderIsMissing() {
        HttpHeaders input = new HttpHeaders();
        input.set("X-Forwarded-For", "2001:4860:4860::8888, 172.18.0.1");
        HttpHeaders headers = filter(input);

        assertThat(headers.getFirst("X-Forwarded-For"))
                .isEqualTo("[2001:4860:4860::8888]");
    }

    private HttpHeaders filter(HttpHeaders headers) {
        MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest.get("/")
                .headers(headers);
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        return filter.filter(headers, exchange);
    }
}

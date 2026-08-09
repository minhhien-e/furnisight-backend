package com.furnisight.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtClaimsToHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof Jwt)
                .cast(Jwt.class)
                .map(jwt -> {
                    String userId = jwt.getSubject();
                    List<String> roles = jwt.getClaimAsStringList("roles");
                    List<String> permissions = jwt.getClaimAsStringList("permissions");
                    Boolean isAdmin = jwt.getClaimAsBoolean("isAdmin");

                    ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

                    if (userId != null) {
                        requestBuilder.header("X-User-Id", userId);
                    }

                    if (roles != null && !roles.isEmpty()) {
                        requestBuilder.header("X-User-Roles", String.join(",", roles));
                    }

                    if (permissions != null && !permissions.isEmpty()) {
                        requestBuilder.header("X-User-Permissions", String.join(",", permissions));
                    }

                    if (isAdmin != null) {
                        requestBuilder.header("X-User-Is-Admin", isAdmin.toString());
                    }

                    return exchange.mutate().request(requestBuilder.build()).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return 0; 
    }
}

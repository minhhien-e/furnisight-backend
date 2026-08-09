package com.furnisight.promotion.adapter.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String permissionsHeader = request.getHeader("X-User-Permissions");

        if (userId != null && !userId.isBlank()) {
            List<SimpleGrantedAuthority> authorities = List.of();
            if (permissionsHeader != null && !permissionsHeader.isBlank()) {
                authorities = Arrays.stream(permissionsHeader.split(","))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .toList();
            }
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, authorities));
        }

        filterChain.doFilter(request, response);
    }
}

package com.furnisight.admin.shared.security;

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
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String permissionsHeader = request.getHeader("X-User-Permissions");
        String isAdminHeader = request.getHeader("X-User-Is-Admin");

        if (userId != null && !userId.isBlank()) {
            List<SimpleGrantedAuthority> authorities = List.of();
            if (permissionsHeader != null && !permissionsHeader.isBlank()) {
                java.util.Set<String> expanded = new java.util.HashSet<>();
                if ("true".equalsIgnoreCase(isAdminHeader)) {
                    expanded.add("ADMIN");
                }
                for (String permission : permissionsHeader.split(",")) {
                    String p = permission.trim().toUpperCase();
                    if (!p.isBlank()) {
                        expanded.add(p);
                    }
                }
                authorities = expanded.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

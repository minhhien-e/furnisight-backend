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

        if (userId != null && !userId.isBlank()) {
            List<SimpleGrantedAuthority> authorities = List.of();
            if (permissionsHeader != null && !permissionsHeader.isBlank()) {
                java.util.Set<String> expanded = new java.util.HashSet<>();
                for (String permission : permissionsHeader.split(",")) {
                    String p = permission.trim().toUpperCase();
                    expanded.add(p);
                    if ("MANAGE_USERS".equals(p)) {
                        expanded.addAll(List.of("MANAGE_USERS", "VIEW_DASHBOARD", "dashboard", "USER_VIEW", "user_view", "USER_MANAGE", "user_manage"));
                    } else if ("MANAGE_ROLES".equals(p)) {
                        expanded.addAll(List.of("MANAGE_ROLES", "ROLE_MANAGE", "role_manage", "PRODUCT_VIEW", "product_view", "PRODUCT_CREATE", "product_create", "PRODUCT_EDIT", "product_edit", "PRODUCT_DELETE", "product_delete", "INVENTORY", "inventory", "REPORTS", "reports"));
                    } else if ("CAN_ORDERS".equals(p)) {
                        expanded.addAll(List.of("CAN_ORDERS", "ORDER_VIEW", "order_view", "ORDER_UPDATE", "order_update", "MANAGE_ORDERS"));
                    } else if ("MANAGE_BANS".equals(p)) {
                        expanded.addAll(List.of("MANAGE_BANS", "BAN_MANAGE", "ban_manage"));
                    }
                }
                authorities = expanded.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

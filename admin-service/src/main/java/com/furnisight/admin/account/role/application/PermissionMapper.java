package com.furnisight.admin.account.role.application;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionMapper {

    private static final Map<String, Set<String>> FE_TO_BACKEND = Map.ofEntries(
            Map.entry("product_manage", Set.of("PRODUCT_MANAGE")),
            Map.entry("order_manage", Set.of("ORDER_MANAGE")),
            Map.entry("voucher_manage", Set.of("VOUCHER_MANAGE")),
            Map.entry("account_manage", Set.of("ACCOUNT_MANAGE")),
            Map.entry("customer_support", Set.of("CUSTOMER_SUPPORT"))
    );


    public Set<String> toBackendPermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Set.of();
        }
        return permissions.stream()
                .flatMap(permission -> FE_TO_BACKEND
                        .getOrDefault(normalize(permission).toLowerCase(Locale.ROOT), Set.of(normalize(permission)))
                        .stream())
                .filter(permission -> Set.of(
                        "PRODUCT_MANAGE", "ORDER_MANAGE", "VOUCHER_MANAGE", "ACCOUNT_MANAGE", "CUSTOMER_SUPPORT").contains(permission))
                .collect(Collectors.toSet());
    }

    public List<String> toFrontendPermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }
        java.util.Set<String> fePermissions = new java.util.HashSet<>();
        for (String permission : permissions) {
            String p = normalize(permission);
            if (Set.of("PRODUCT_MANAGE", "ORDER_MANAGE", "VOUCHER_MANAGE", "ACCOUNT_MANAGE", "CUSTOMER_SUPPORT").contains(p)) {
                fePermissions.add(p.toLowerCase(Locale.ROOT));
            }
        }
        return fePermissions.stream().sorted().toList();
    }

    private String normalize(String permission) {
        return permission == null ? "" : permission.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }
}

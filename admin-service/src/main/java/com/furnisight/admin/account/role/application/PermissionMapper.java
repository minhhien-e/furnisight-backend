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
            Map.entry("dashboard", Set.of("MANAGE_USERS")),
            Map.entry("user_view", Set.of("MANAGE_USERS")),
            Map.entry("user_manage", Set.of("MANAGE_USERS")),
            Map.entry("role_manage", Set.of("MANAGE_ROLES")),
            Map.entry("ban_manage", Set.of("MANAGE_BANS")),
            Map.entry("order_view", Set.of("CAN_ORDERS")),
            Map.entry("order_update", Set.of("CAN_ORDERS")),
            Map.entry("product_create", Set.of("MANAGE_ROLES")),
            Map.entry("product_edit", Set.of("MANAGE_ROLES")),
            Map.entry("product_delete", Set.of("MANAGE_ROLES")),
            Map.entry("inventory", Set.of("MANAGE_ROLES")),
            Map.entry("reports", Set.of("MANAGE_ROLES"))
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
                        "MANAGE_USERS", "MANAGE_ROLES", "MANAGE_BANS", "CAN_ORDERS").contains(permission))
                .collect(Collectors.toSet());
    }

    public List<String> toFrontendPermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }
        java.util.Set<String> fePermissions = new java.util.HashSet<>();
        for (String permission : permissions) {
            String p = normalize(permission);
            if ("MANAGE_USERS".equals(p)) {
                fePermissions.addAll(List.of("dashboard", "user_view", "user_manage"));
            } else if ("MANAGE_ROLES".equals(p)) {
                fePermissions.addAll(List.of("role_manage", "product_create", "product_edit", "product_delete", "inventory", "reports"));
            } else if ("CAN_ORDERS".equals(p)) {
                fePermissions.addAll(List.of("order_view", "order_update"));
            } else if ("MANAGE_BANS".equals(p)) {
                fePermissions.add("ban_manage");
            } else {
                fePermissions.add(p.toLowerCase(Locale.ROOT));
            }
        }
        return fePermissions.stream().sorted().toList();
    }

    private String normalize(String permission) {
        return permission == null ? "" : permission.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }
}

package com.furnisight.user.domain.enums.identity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum Permission {
    PRODUCT_MANAGE(1L),
    ORDER_MANAGE(1L << 1),
    VOUCHER_MANAGE(1L << 2),
    ACCOUNT_MANAGE(1L << 3),
    CUSTOMER_SUPPORT(1L << 4);

    private final long bit;

    Permission(long bit) {
        this.bit = bit;
    }

    public static boolean hasPermission(long currentPermissions, Permission requiredPermission) {
        return (currentPermissions & requiredPermission.getBit()) != 0;
    }

    public static long addPermission(long currentPermissions, Permission permissionToAdd) {
        return currentPermissions | permissionToAdd.getBit();
    }

    public static long removePermission(long currentPermissions, Permission permissionToRemove) {
        return currentPermissions & ~permissionToRemove.getBit();
    }

    public static List<Permission> getPermissions(long currentPermissions) {
        List<Permission> result = new ArrayList<>();

        for (Permission permission : Permission.values()) {
            if (hasPermission(currentPermissions, permission)) {
                result.add(permission);
            }
        }

        return result;
    }
}

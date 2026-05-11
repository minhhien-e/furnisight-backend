package com.furnisight.user.domain.enums.identity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum Permission {
    // 1 << 0 = 1
    MANAGE_USERS(1L),
    // 1 << 1 = 2
    MANAGE_ROLES(1L << 1),
    // 1 << 2 = 4
    MANAGE_BANS(1L << 2),
    // 1 << 3 = 8
    CAN_ORDERS(1L << 3);

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

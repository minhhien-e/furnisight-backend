package com.furnisight.admin.controller.dto;

import java.util.List;

public record SaveAdminRoleRequest(
        String name,
        String description,
        List<String> permissions,
        Integer position
) {
}

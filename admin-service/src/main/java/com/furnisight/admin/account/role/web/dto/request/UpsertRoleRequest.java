package com.furnisight.admin.account.role.web.dto.request;

import java.util.List;

public record UpsertRoleRequest(
        String name,
        String description,
        List<String> permissions,
        Integer position
) {
}

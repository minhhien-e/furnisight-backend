package com.furnisight.admin.account.role.web.dto.response;

import java.util.List;

public record RoleResponse(
        String id,
        String name,
        List<String> permissions
) {
}

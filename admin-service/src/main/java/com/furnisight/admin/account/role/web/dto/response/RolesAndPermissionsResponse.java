package com.furnisight.admin.account.role.web.dto.response;

import java.util.List;

public record RolesAndPermissionsResponse(
        List<RoleResponse> roles,
        List<String> permissions
) {
}

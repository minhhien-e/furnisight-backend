package com.furnisight.admin.account.role.web.dto.response;

import java.util.List;

public record RoleListResponse(
        List<RoleResponse> roles
) {
}

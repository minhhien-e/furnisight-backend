package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminRoleListResponse(
        List<AdminRoleResponse> roles
) {
}

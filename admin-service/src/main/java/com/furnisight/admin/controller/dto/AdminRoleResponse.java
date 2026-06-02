package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminRoleResponse(
        String id,
        String name,
        List<String> permissions
) {
}

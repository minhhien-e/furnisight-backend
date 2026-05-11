package com.furnisight.user.presentation.web.rest.dto.request.role;

public record UpdateRoleRequest(
    String name,
    int position
) {}

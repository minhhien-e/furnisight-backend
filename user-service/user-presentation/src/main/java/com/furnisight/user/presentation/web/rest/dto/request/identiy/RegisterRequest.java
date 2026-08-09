package com.furnisight.user.presentation.web.rest.dto.request.identiy;

public record RegisterRequest(
        String email,
        String password,
        String fullName
) {}

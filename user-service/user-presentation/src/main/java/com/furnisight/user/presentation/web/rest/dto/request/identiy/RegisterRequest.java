package com.furnisight.user.presentation.web.rest.dto.request.identiy;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String firstName,
        String lastName
) {}

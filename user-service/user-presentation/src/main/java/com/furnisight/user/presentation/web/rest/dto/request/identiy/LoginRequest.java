package com.furnisight.user.presentation.web.rest.dto.request.identiy;

public record LoginRequest(
    String identifier,
    String password
) {}

package com.furnisight.user.presentation.web.rest.dto.request.identiy;

public record ForgotPasswordRequest(
    String channel,
    String destination
) {}

package com.furnisight.user.application.token.dto;

public record RenewAccessTokenCommand (
    String refreshToken
) {}

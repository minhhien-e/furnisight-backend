package com.furnisight.user.application.account.dto;

public record RegisterAccountCommand(
        String email,
        String password,
        String fullName
) {}

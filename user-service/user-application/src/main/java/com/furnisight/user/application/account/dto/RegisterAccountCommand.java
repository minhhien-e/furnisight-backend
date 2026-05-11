package com.furnisight.user.application.account.dto;

public record RegisterAccountCommand(
        String username,
        String email,
        String password,
        String firstName,
        String lastName
) {}

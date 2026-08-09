package com.furnisight.user.application.account.dto;

public record LoginAccountCommand(
    String identifier,
    String password
) {}

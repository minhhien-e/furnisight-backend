package com.furnisight.user.application.account.dto;

public record VerifyAccountCommand(
        String otpCode) {
}

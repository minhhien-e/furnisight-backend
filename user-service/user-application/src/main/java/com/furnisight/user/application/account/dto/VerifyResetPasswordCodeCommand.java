package com.furnisight.user.application.account.dto;

public record VerifyResetPasswordCodeCommand(String email, String token) {
}

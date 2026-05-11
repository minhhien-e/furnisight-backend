package com.furnisight.user.application.account.dto;

public record ForgotPasswordCommand(
    String channel,
    String destination
) {
}

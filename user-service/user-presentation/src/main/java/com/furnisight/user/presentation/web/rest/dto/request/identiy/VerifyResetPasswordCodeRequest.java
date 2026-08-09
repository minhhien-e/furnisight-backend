package com.furnisight.user.presentation.web.rest.dto.request.identiy;

public record VerifyResetPasswordCodeRequest(String email, String code) {
}

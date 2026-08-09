package com.furnisight.user.presentation.web.rest.controller.identity;

import com.furnisight.user.application.account.dto.*;
import com.furnisight.user.application.account.port.in.usecase.*;
import com.furnisight.user.application.common.port.in.CurrentUserProvider;
import com.furnisight.user.application.token.dto.RenewAccessTokenCommand;
import com.furnisight.user.application.token.port.in.usecase.RenewAccessTokenUseCase;
import com.furnisight.user.presentation.web.rest.dto.request.identiy.*;



import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterAccountUseCase registerAccountUseCase;
    private final LoginAccountUseCase loginAccountUseCase;
    private final LogoutAccountUseCase logoutAccountUseCase;
    private final LogoutAllAccountUseCase logoutAllAccountUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final VerifyResetPasswordCodeUseCase verifyResetPasswordCodeUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final DeleteOwnAccountUseCase deleteOwnAccountUseCase;
    private final RenewAccessTokenUseCase renewAccessTokenUseCase;
    private final VerifyAccountUseCase verifyAccountUseCase;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        var command = new RegisterAccountCommand(
            request.email(),
            request.password(),
            request.fullName()
        );
        var result = registerAccountUseCase.execute(command);
        return ResponseEntity.ok(com.furnisight.user.presentation.web.rest.dto.response.AuthResponse.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        var command = new LoginAccountCommand(request.identifier(), request.password());
        var result = loginAccountUseCase.execute(command);
        return ResponseEntity.ok(com.furnisight.user.presentation.web.rest.dto.response.AuthResponse.from(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutAccountRequest request) {
        var command = new LogoutAccountCommand(request.refreshToken());
        var result = logoutAccountUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll() {
        UUID accountId = currentUserProvider.getCurrentUserId();
        var command = new LogoutAllAccountCommand(accountId);
        var result = logoutAllAccountUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        var command = new ChangePasswordCommand(accountId, request.currentPassword(), request.newPassword());
        var result = changePasswordUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        var command = new ForgotPasswordCommand(request.channel(), request.destination());
        var result = forgotPasswordUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password/verify")
    public ResponseEntity<?> verifyResetPasswordCode(@RequestBody VerifyResetPasswordCodeRequest request) {
        var command = new VerifyResetPasswordCodeCommand(request.email(), request.code());
        var result = verifyResetPasswordCodeUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        var command = new ResetPasswordCommand(request.email(), request.token(), request.newPassword());
        var result = resetPasswordUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(
        @RequestParam String otpCode
    ) {
        var command = new VerifyAccountCommand(otpCode);
        var result = verifyAccountUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> renewAccessToken(@RequestBody RenewAccessTokenRequest request) {
        var command = new RenewAccessTokenCommand(request.refreshToken());
        var result = renewAccessTokenUseCase.execute(command);
        return ResponseEntity.ok(com.furnisight.user.presentation.web.rest.dto.response.AuthResponse.from(result));
    }

    @GetMapping("/login/google")
    public ResponseEntity<?> googleLogin() {
        String authorizationUrl = "/oauth2/authorization/google";
        return ResponseEntity.ok(java.util.Map.of("redirectUrl", authorizationUrl));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteOwnAccount() {
        UUID accountId = currentUserProvider.getCurrentUserId();
        var command = new DeleteOwnAccountCommand(accountId);
        var result = deleteOwnAccountUseCase.execute(command);
        return ResponseEntity.ok(result);
    }
}

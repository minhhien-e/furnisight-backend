package com.furnisight.user.presentation.web.rest.controller.identity;

import com.furnisight.user.application.account.dto.*;
import com.furnisight.user.application.account.port.in.usecase.*;
import com.furnisight.user.presentation.web.rest.dto.request.identiy.AssignRoleRequest;
import com.furnisight.user.presentation.web.rest.dto.request.identiy.BanAccountRequest;
import com.furnisight.user.presentation.web.rest.dto.request.identiy.RevokeRoleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import com.furnisight.user.application.common.port.in.CurrentUserProvider;

@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final ActivateAccountUseCase activateAccountUseCase;
    private final BanAccountUseCase banAccountUseCase;
    private final UnbanAccountUseCase unbanAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final AssignRoleUseCase assignRoleUseCase;
    private final RevokeRoleUseCase revokeRoleUseCase;
    private final CurrentUserProvider currentUserProvider;

    @PutMapping("/{accountId}/activate")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<?> activateAccount(
            @PathVariable UUID accountId) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        var command = new ActivateAccountCommand(adminId, accountId);
        var result = activateAccountUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{accountId}/ban")
    @PreAuthorize("hasAuthority('MANAGE_BANS')")
    public ResponseEntity<?> banAccount(
            @PathVariable UUID accountId,
            @RequestBody BanAccountRequest request) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        var command = new BanAccountCommand(adminId, accountId, request.reason(), request.expiresAt());
        var result = banAccountUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{accountId}/unban")
    @PreAuthorize("hasAuthority('MANAGE_BANS')")
    public ResponseEntity<?> unbanAccount(
            @PathVariable UUID accountId) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        var command = new UnbanAccountCommand(adminId, accountId);
        var result = unbanAccountUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<?> deleteAccount(
            @PathVariable UUID accountId) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        var command = new DeleteAccountCommand(adminId, accountId);
        var result = deleteAccountUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{accountId}/roles/assign")
    // @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<?> assignRole(
            @PathVariable UUID accountId,
            @RequestBody AssignRoleRequest request) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        var command = new AssignRoleCommand(adminId, accountId, request.roleId());
        var result = assignRoleUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{accountId}/roles/revoke")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<?> revokeRole(
            @PathVariable UUID accountId,
            @RequestBody RevokeRoleRequest request) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        var command = new RevokeRoleCommand(adminId, accountId, request.roleId());
        var result = revokeRoleUseCase.execute(command);
        return ResponseEntity.ok(result);
    }
}

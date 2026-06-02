package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminAccountDetailResponse;
import com.furnisight.admin.controller.dto.AdminAccountPageResponse;
import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminRoleListResponse;
import com.furnisight.admin.security.CurrentUserProvider;
import com.furnisight.admin.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.furnisight.admin.controller.dto.CreateUserRequest;
import com.furnisight.admin.controller.dto.UpdateAdminUserRequest;
import com.furnisight.admin.controller.dto.UpdateUserRoleRequest;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('USER_VIEW') or hasAuthority('user_view')")
    public ResponseEntity<AdminAccountPageResponse> getAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(adminAccountService.getAccounts(page, size, query, status));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('USER_VIEW') or hasAuthority('user_view')")
    public ResponseEntity<AdminAccountDetailResponse> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminAccountService.getAccountById(id));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('MANAGE_ROLES') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminRoleListResponse> getRoles() {
        return ResponseEntity.ok(adminAccountService.getRoles());
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> createUser(@RequestBody CreateUserRequest request) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(adminAccountService.createAccount(adminId, request));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> updateUser(@PathVariable UUID id,
            @RequestBody UpdateAdminUserRequest request) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(adminAccountService.updateAccount(adminId, id, request));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> deleteUser(@PathVariable UUID id) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(adminAccountService.deleteAccount(adminId, id));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<AdminActionResultResponse> updateUserRole(@PathVariable UUID id,
            @RequestBody UpdateUserRoleRequest request) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        if (request.getRoleId() != null) {
            if ("REVOKE".equalsIgnoreCase(request.getAction())) {
                return ResponseEntity.ok(adminAccountService.revokeRole(adminId, id, request.getRoleId()));
            }
            return ResponseEntity.ok(adminAccountService.assignRole(adminId, id, request.getRoleId()));
        }
        return ResponseEntity.badRequest().body(new AdminActionResultResponse(false, "roleId cannot be empty"));
    }
}

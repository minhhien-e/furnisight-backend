package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminAccountDetailResponse;
import com.furnisight.admin.controller.dto.AdminAccountPageResponse;
import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminRoleListResponse;
import com.furnisight.admin.security.CurrentUserProvider;
import com.furnisight.admin.service.AdminAccountService;
import com.furnisight.admin.service.AdminAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.furnisight.admin.controller.dto.CreateUserRequest;
import com.furnisight.admin.controller.dto.SaveAdminRoleRequest;
import com.furnisight.admin.controller.dto.UpdateAdminUserRequest;
import com.furnisight.admin.controller.dto.UpdateUserRoleRequest;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;
    private final CurrentUserProvider currentUserProvider;
    private final AdminAuditLogService adminAuditLogService;

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

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('MANAGE_ROLES') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> createRole(@RequestBody SaveAdminRoleRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        AdminActionResultResponse result = adminAccountService.createRole(request);
        adminAuditLogService.record(adminId, "create", "Tạo vai trò", "ROLE", request.name(), result,
                "Tên vai trò: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> updateRole(@PathVariable String id,
            @RequestBody SaveAdminRoleRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        AdminActionResultResponse result = adminAccountService.updateRole(id, request);
        adminAuditLogService.record(adminId, "update", "Cập nhật vai trò", "ROLE", id, result,
                "Tên vai trò: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> deleteRole(@PathVariable String id, HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        AdminActionResultResponse result = adminAccountService.deleteRole(id);
        adminAuditLogService.record(adminId, "delete", "Xóa vai trò", "ROLE", id, result,
                "Role id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> createUser(@RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        AdminActionResultResponse result = adminAccountService.createAccount(adminId, request);
        adminAuditLogService.record(adminId, "create", "Tạo tài khoản admin", "USER", request.getEmail(), result,
                "Email: " + request.getEmail(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> updateUser(@PathVariable UUID id,
            @RequestBody UpdateAdminUserRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        AdminActionResultResponse result = adminAccountService.updateAccount(adminId, id, request);
        adminAuditLogService.record(adminId, "update", "Cập nhật tài khoản", "USER", id.toString(), result,
                "User id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminActionResultResponse> deleteUser(@PathVariable UUID id, HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        AdminActionResultResponse result = adminAccountService.deleteAccount(adminId, id);
        adminAuditLogService.record(adminId, "delete", "Xóa tài khoản", "USER", id.toString(), result,
                "User id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<AdminActionResultResponse> updateUserRole(@PathVariable UUID id,
            @RequestBody UpdateUserRoleRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        if (request.getRoleId() != null) {
            AdminActionResultResponse result;
            if ("REVOKE".equalsIgnoreCase(request.getAction())) {
                result = adminAccountService.revokeRole(adminId, id, request.getRoleId());
                adminAuditLogService.record(adminId, "update", "Gỡ vai trò người dùng", "USER_ROLE", id.toString(), result,
                        "Role id: " + request.getRoleId(), httpRequest);
                return ResponseEntity.ok(result);
            }
            result = adminAccountService.assignRole(adminId, id, request.getRoleId());
            adminAuditLogService.record(adminId, "update", "Gán vai trò người dùng", "USER_ROLE", id.toString(), result,
                    "Role id: " + request.getRoleId(), httpRequest);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(new AdminActionResultResponse(false, "roleId cannot be empty"));
    }
}

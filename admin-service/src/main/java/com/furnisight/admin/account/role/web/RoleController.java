package com.furnisight.admin.account.role.web;

import com.furnisight.admin.account.role.application.RoleService;
import com.furnisight.admin.account.role.web.dto.request.UpdateUserRoleRequest;
import com.furnisight.admin.account.role.web.dto.request.UpsertRoleRequest;
import com.furnisight.admin.account.role.web.dto.response.RoleListResponse;
import com.furnisight.admin.account.role.web.dto.response.RolesAndPermissionsResponse;
import com.furnisight.admin.audit.application.AuditLogService;
import com.furnisight.admin.shared.security.CurrentUserProvider;
import com.furnisight.admin.shared.web.ActionResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogService auditLogService;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('MANAGE_ROLES') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<RoleListResponse> getRoles() {
        return ResponseEntity.ok(roleService.getRoles());
    }

    @GetMapping("/roles/permissions")
    @PreAuthorize("hasAuthority('MANAGE_ROLES') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<RolesAndPermissionsResponse> getRolesAndPermissions() {
        return ResponseEntity.ok(roleService.getRolesAndPermissions());
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('MANAGE_ROLES') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> createRole(
            @RequestBody UpsertRoleRequest request, HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = roleService.createRole(request);
        auditLogService.record(adminId, "create", "Tạo vai trò", "ROLE",
                request.name(), result, "Tên vai trò: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> updateRole(
            @PathVariable String id, @RequestBody UpsertRoleRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = roleService.updateRole(id, request);
        auditLogService.record(adminId, "update", "Cập nhật vai trò", "ROLE",
                id, result, "Tên vai trò: " + request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ActionResultResponse> deleteRole(
            @PathVariable String id, HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = roleService.deleteRole(id);
        auditLogService.record(adminId, "delete", "Xóa vai trò", "ROLE",
                id, result, "Role id: " + id, httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ActionResultResponse> updateUserRole(
            @PathVariable UUID id, @RequestBody UpdateUserRoleRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        if (request.getRoleId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ActionResultResponse(false, "roleId cannot be empty"));
        }

        if ("REVOKE".equalsIgnoreCase(request.getAction())) {
            ActionResultResponse result = roleService.revokeRole(adminId, id, request.getRoleId());
            auditLogService.record(adminId, "update", "Gỡ vai trò người dùng", "USER_ROLE",
                    id.toString(), result, "Role id: " + request.getRoleId(), httpRequest);
            return ResponseEntity.ok(result);
        }

        ActionResultResponse result = roleService.assignRole(adminId, id, request.getRoleId());
        auditLogService.record(adminId, "update", "Gán vai trò người dùng", "USER_ROLE",
                id.toString(), result, "Role id: " + request.getRoleId(), httpRequest);
        return ResponseEntity.ok(result);
    }
}

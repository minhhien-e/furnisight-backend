package com.furnisight.admin.account.role.web;

import com.furnisight.admin.account.role.application.RoleService;
import com.furnisight.admin.account.role.web.dto.request.UpdateUserRoleRequest;
import com.furnisight.admin.account.role.web.dto.request.UpsertRoleRequest;
import com.furnisight.admin.account.role.web.dto.response.RoleResponse;
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
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogService auditLogService;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(roleService.getRoles());
    }

    @GetMapping("/roles/permissions")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<RolesAndPermissionsResponse> getRolesAndPermissions() {
        return ResponseEntity.ok(roleService.getRolesAndPermissions());
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> createRole(
            @RequestBody UpsertRoleRequest request, HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = roleService.createRole(request);
        auditLogService.record(adminId, com.furnisight.admin.audit.domain.AuditAction.CREATE_ROLE,
                request.name(), result, "", httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> updateRole(
            @PathVariable String id, @RequestBody UpsertRoleRequest request,
            HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = roleService.updateRole(id, request);
        auditLogService.record(adminId, com.furnisight.admin.audit.domain.AuditAction.UPDATE_ROLE,
                id, result, request.name(), httpRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGE') or hasAuthority('ADMIN')")
    public ResponseEntity<ActionResultResponse> deleteRole(
            @PathVariable String id, HttpServletRequest httpRequest) {
        UUID adminId = currentUserProvider.getCurrentUserId();
        ActionResultResponse result = roleService.deleteRole(id);
        auditLogService.record(adminId, com.furnisight.admin.audit.domain.AuditAction.DELETE_ROLE,
                id, result, "", httpRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGE') or hasAuthority('ADMIN')")
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
            auditLogService.record(adminId, com.furnisight.admin.audit.domain.AuditAction.REVOKE_ROLE,
                    id.toString(), result, request.getRoleId().toString(), httpRequest);
            return ResponseEntity.ok(result);
        }

        ActionResultResponse result = roleService.assignRole(adminId, id, request.getRoleId());
        auditLogService.record(adminId, com.furnisight.admin.audit.domain.AuditAction.ASSIGN_ROLE,
                id.toString(), result, request.getRoleId().toString(), httpRequest);
        return ResponseEntity.ok(result);
    }
}

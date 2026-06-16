package com.furnisight.admin.account.role.application;

import com.furnisight.admin.account.infrastructure.grpc.AdminUserGrpcClient;
import com.furnisight.admin.account.role.web.dto.request.UpsertRoleRequest;
import com.furnisight.admin.account.role.domain.Permission;
import com.furnisight.admin.account.role.web.dto.response.RoleListResponse;
import com.furnisight.admin.account.role.web.dto.response.RoleResponse;
import com.furnisight.admin.account.role.web.dto.response.RolesAndPermissionsResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.user.AdminActionResponse;
import com.furnisight.admin.user.RoleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final AdminUserGrpcClient userClient;
    private final PermissionMapper permissionMapper;

    public RoleListResponse getRoles() {
        return new RoleListResponse(toRoleResponses(userClient.getRoles().getRolesList()));
    }

    public RolesAndPermissionsResponse getRolesAndPermissions() {
        List<RoleResponse> roles = toRoleResponses(userClient.getRoles().getRolesList());
        List<String> permissions = Stream.of(Permission.values())
                .map(Enum::name)
                .toList();
        return new RolesAndPermissionsResponse(roles, permissions);
    }

    public ActionResultResponse createRole(UpsertRoleRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            return new ActionResultResponse(false, "Role name cannot be empty");
        }
        String name = request.name().trim();
        com.furnisight.admin.user.RoleListResponse before = userClient.getRoles();
        if (before.getRolesList().stream().anyMatch(role -> role.getName().equalsIgnoreCase(name))) {
            return new ActionResultResponse(false, "Role already exists");
        }
        return toActionResult(userClient.createRole(
                name,
                resolvePosition(request, before.getRolesCount() + 1),
                List.copyOf(permissionMapper.toBackendPermissions(request.permissions()))));
    }

    public ActionResultResponse updateRole(String id, UpsertRoleRequest request) {
        UUID roleId = parseUuid(id);
        if (roleId == null) {
            return new ActionResultResponse(false, "Invalid role id");
        }
        return toActionResult(userClient.updateRole(
                id, request.name(), resolvePosition(request, 1),
                List.copyOf(permissionMapper.toBackendPermissions(request.permissions()))));
    }

    public ActionResultResponse deleteRole(String id) {
        UUID roleId = parseUuid(id);
        if (roleId == null) {
            return new ActionResultResponse(false, "Invalid role id");
        }
        return toActionResult(userClient.deleteRole(roleId.toString()));
    }

    public ActionResultResponse assignRole(UUID adminId, UUID accountId, UUID roleId) {
        return toActionResult(userClient.assignRole(adminId, accountId, roleId));
    }

    public ActionResultResponse revokeRole(UUID adminId, UUID accountId, UUID roleId) {
        return toActionResult(userClient.revokeRole(adminId, accountId, roleId));
    }

    public List<RoleResponse> toRoleResponses(List<RoleDto> roles) {
        return roles.stream()
                .map(role -> new RoleResponse(
                        role.getId(), role.getName(),
                        permissionMapper.toFrontendPermissions(role.getPermissionsList())))
                .toList();
    }

    private ActionResultResponse toActionResult(AdminActionResponse response) {
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private int resolvePosition(UpsertRoleRequest request, int fallback) {
        return request.position() == null || request.position() <= 0 ? fallback : request.position();
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}

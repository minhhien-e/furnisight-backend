package com.furnisight.admin.account.role.application;

import com.furnisight.admin.account.infrastructure.grpc.AdminUserGrpcClient;
import com.furnisight.admin.account.role.web.dto.request.UpsertRoleRequest;
import com.furnisight.admin.account.role.domain.Permission;
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

    public List<RoleResponse> getRoles() {
        return toRoleResponses(userClient.getRoles().getRolesList());
    }

    public RolesAndPermissionsResponse getRolesAndPermissions() {
        List<RoleResponse> roles = toRoleResponses(userClient.getRoles().getRolesList());
        List<String> permissions = Stream.of(Permission.values())
                .map(Enum::name)
                .toList();
        return new RolesAndPermissionsResponse(roles, permissions);
    }

    public ActionResultResponse createRole(UpsertRoleRequest request) {
        String name = request.name().trim();
        return toActionResult(userClient.createRole(
                name,
                request.position() != null ? request.position() : 0,
                request.permissions() == null ? List.of() : List.copyOf(request.permissions())));
    }

    public ActionResultResponse updateRole(String id, UpsertRoleRequest request) {
        return toActionResult(userClient.updateRole(
                id, request.name(), 
                request.position() != null ? request.position() : 0,
                request.permissions() == null ? List.of() : List.copyOf(request.permissions())));
    }

    public ActionResultResponse deleteRole(String id) {
        return toActionResult(userClient.deleteRole(id));
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
                        role.getPermissionsList() == null ? List.of() : role.getPermissionsList()))
                .toList();
    }

    private ActionResultResponse toActionResult(AdminActionResponse response) {
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

}

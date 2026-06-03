package com.furnisight.admin.service;

import com.furnisight.admin.controller.dto.AdminAccountDetailResponse;
import com.furnisight.admin.controller.dto.AdminAccountPageResponse;
import com.furnisight.admin.controller.dto.AdminAccountSummaryResponse;
import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminRoleListResponse;
import com.furnisight.admin.controller.dto.AdminRoleResponse;
import com.furnisight.admin.controller.dto.CreateUserRequest;
import com.furnisight.admin.controller.dto.SaveAdminRoleRequest;
import com.furnisight.admin.integration.GrpcAdminUserClient;
import com.furnisight.admin.controller.dto.UpdateAdminUserRequest;
import com.furnisight.admin.user.AccountDetailResponse;
import com.furnisight.admin.user.AccountDto;
import com.furnisight.admin.user.AdminActionResponse;
import com.furnisight.admin.user.AccountPageResponse;
import com.furnisight.admin.user.RoleDto;
import com.furnisight.admin.user.RoleListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final GrpcAdminUserClient grpcAdminUserClient;

    private static final Map<String, Set<String>> FE_TO_BACKEND_PERMISSIONS = Map.ofEntries(
            Map.entry("dashboard", Set.of("MANAGE_USERS")),
            Map.entry("user_view", Set.of("MANAGE_USERS")),
            Map.entry("user_manage", Set.of("MANAGE_USERS")),
            Map.entry("role_manage", Set.of("MANAGE_ROLES")),
            Map.entry("ban_manage", Set.of("MANAGE_BANS")),
            Map.entry("order_view", Set.of("CAN_ORDERS")),
            Map.entry("order_update", Set.of("CAN_ORDERS")),
            Map.entry("product_create", Set.of("MANAGE_ROLES")),
            Map.entry("product_edit", Set.of("MANAGE_ROLES")),
            Map.entry("product_delete", Set.of("MANAGE_ROLES")),
            Map.entry("inventory", Set.of("MANAGE_ROLES")),
            Map.entry("reports", Set.of("MANAGE_ROLES"))
    );

    private static final Map<String, String> BACKEND_TO_FE_PERMISSION = Map.of(
            "MANAGE_USERS", "user_view",
            "MANAGE_ROLES", "role_manage",
            "MANAGE_BANS", "ban_manage",
            "CAN_ORDERS", "order_view"
    );

    public AdminAccountPageResponse getAccounts(int page, int size, String query, String status) {
        AccountPageResponse response = grpcAdminUserClient.getAccounts(page, size, query, status);

        List<AdminAccountSummaryResponse> accounts = response.getAccountsList().stream()
                .map(this::toAccountSummaryResponse)
                .toList();

        return new AdminAccountPageResponse(
                accounts,
                response.getTotalPages(),
                response.getTotalElements(),
                response.getCurrentPage()
        );
    }

    public AdminAccountDetailResponse getAccountById(UUID id) {
        AccountDetailResponse account = grpcAdminUserClient.getAccountById(id);
        String firstName = account.getFirstName();
        String lastName = account.getLastName();
        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isBlank()) {
            fullName = account.getUsername();
        }

        return new AdminAccountDetailResponse(
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                toAccountTone(account.getStatus()),
                toAccountStatusLabel(account.getStatus()),
                formatDate(account.getCreatedAt()),
                firstName,
                lastName,
                fullName,
                account.getAvatarUrl(),
                toRoleResponses(account.getRolesList())
        );
    }

    public AdminRoleListResponse getRoles() {
        RoleListResponse response = grpcAdminUserClient.getRoles();
        return new AdminRoleListResponse(toRoleResponses(response.getRolesList()));
    }

    public AdminActionResultResponse createRole(SaveAdminRoleRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            return new AdminActionResultResponse(false, "Role name cannot be empty");
        }
        String name = request.name().trim();
        RoleListResponse before = grpcAdminUserClient.getRoles();
        if (before.getRolesList().stream().anyMatch(role -> role.getName().equalsIgnoreCase(name))) {
            return new AdminActionResultResponse(false, "Role already exists");
        }

        return toActionResultResponse(grpcAdminUserClient.createRole(
                name,
                resolvePosition(request, before.getRolesCount() + 1),
                List.copyOf(toBackendPermissions(request.permissions()))));
    }

    public AdminActionResultResponse updateRole(String id, SaveAdminRoleRequest request) {
        UUID roleId = parseUuid(id);
        if (roleId == null) {
            return new AdminActionResultResponse(false, "Invalid role id");
        }
        return toActionResultResponse(grpcAdminUserClient.updateRole(
                id,
                request.name(),
                resolvePosition(request, 1),
                List.copyOf(toBackendPermissions(request.permissions()))));
    }

    public AdminActionResultResponse deleteRole(String id) {
        UUID roleId = parseUuid(id);
        if (roleId == null) {
            return new AdminActionResultResponse(false, "Invalid role id");
        }
        return toActionResultResponse(grpcAdminUserClient.deleteRole(roleId.toString()));
    }

    // Logic for updating user status (e.g. ban/unban) based on payload from FE
    public AdminActionResultResponse updateUserStatus(UUID adminId, UUID accountId, String status) {
        if ("BANNED".equalsIgnoreCase(status)) {
            return toActionResultResponse(grpcAdminUserClient.banAccount(adminId, accountId, "Banned by Admin"));
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            return toActionResultResponse(grpcAdminUserClient.unbanAccount(adminId, accountId));
        }
        return new AdminActionResultResponse(false, "Invalid status update");
    }

    public AdminActionResultResponse updateAccount(UUID adminId, UUID accountId, UpdateAdminUserRequest request) {
        boolean changed = false;

        if (request.getName() != null && !request.getName().isBlank()) {
            NameParts nameParts = splitName(request.getName());
            AdminActionResultResponse profileResult = toActionResultResponse(grpcAdminUserClient.updateAccountProfile(
                    accountId,
                    request.getName(),
                    nameParts.firstName(),
                    nameParts.lastName()));
            if (!profileResult.success()) {
                return profileResult;
            }
            changed = true;
        }

        UUID roleId = parseUuid(firstPresent(request.getRoleId(), request.getRole()));
        if (roleId != null) {
            AdminActionResultResponse roleResult = assignRole(adminId, accountId, roleId);
            if (!roleResult.success()) {
                return roleResult;
            }
            changed = true;
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            AdminActionResultResponse statusResult = updateUserStatus(adminId, accountId, request.getStatus());
            if (!statusResult.success()) {
                return statusResult;
            }
            changed = true;
        }

        if (!changed) {
            return new AdminActionResultResponse(false, "No supported user fields to update");
        }
        return new AdminActionResultResponse(true, "User updated successfully");
    }

    public AdminActionResultResponse assignRole(UUID adminId, UUID accountId, UUID roleId) {
        return toActionResultResponse(grpcAdminUserClient.assignRole(adminId, accountId, roleId));
    }

    public AdminActionResultResponse revokeRole(UUID adminId, UUID accountId, UUID roleId) {
        return toActionResultResponse(grpcAdminUserClient.revokeRole(adminId, accountId, roleId));
    }

    public AdminActionResultResponse deleteAccount(UUID adminId, UUID accountId) {
        return toActionResultResponse(grpcAdminUserClient.deleteAccount(adminId, accountId));
    }

    public AdminActionResultResponse createAccount(UUID adminId, CreateUserRequest request) {
        UUID roleId = parseUuid(request.getRole());

        return toActionResultResponse(grpcAdminUserClient.createAccount(adminId, request.getEmail(), request.getName(), request.getPhone(),
                request.getPassword(), roleId));
    }

    private AdminAccountSummaryResponse toAccountSummaryResponse(AccountDto account) {
        List<AdminRoleResponse> roles = toRoleResponses(account.getRolesList());
        String roleLabel = roles.stream()
                .map(AdminRoleResponse::name)
                .collect(Collectors.joining(", "));
        String name = account.getName().isBlank() ? account.getUsername() : account.getName();

        return new AdminAccountSummaryResponse(
                account.getId(),
                name,
                account.getEmail(),
                toAccountTone(account.getStatus()),
                toAccountStatusLabel(account.getStatus()),
                roleLabel.isBlank() ? "User" : roleLabel,
                roles,
                account.getPhone(),
                formatDate(account.getCreatedAt()),
                "blue",
                name.isBlank() ? "U" : name.substring(0, 1).toUpperCase()
        );
    }

    private String toAccountTone(String status) {
        return switch (normalizeStatus(status)) {
            case "ACTIVE" -> "active";
            case "BANNED", "LOCKED" -> "blocked";
            default -> "inactive";
        };
    }

    private String toAccountStatusLabel(String status) {
        return switch (normalizeStatus(status)) {
            case "ACTIVE" -> "Hoạt động";
            case "BANNED" -> "Bị khóa";
            case "LOCKED" -> "Tạm khóa";
            case "UNVERIFIED" -> "Chưa xác thực";
            default -> "Không hoạt động";
        };
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
    }

    private String formatDate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ignored) {
            return value;
        }
    }

    private List<AdminRoleResponse> toRoleResponses(List<RoleDto> roles) {
        return roles.stream()
                .map(role -> new AdminRoleResponse(
                        role.getId(),
                        role.getName(),
                        toFePermissions(role.getPermissionsList())
                ))
                .toList();
    }

    private AdminActionResultResponse toActionResultResponse(AdminActionResponse response) {
        return new AdminActionResultResponse(response.getSuccess(), response.getMessage());
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

    private String firstPresent(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private NameParts splitName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isBlank()) {
            return new NameParts("", "");
        }
        String[] parts = name.split("\\s+", 2);
        if (parts.length == 1) {
            return new NameParts(parts[0], "");
        }
        return new NameParts(parts[0], parts[1]);
    }

    private int resolvePosition(SaveAdminRoleRequest request, int fallback) {
        return request.position() == null || request.position() <= 0 ? fallback : request.position();
    }

    private Set<String> toBackendPermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Set.of();
        }
        return permissions.stream()
                .flatMap(permission -> FE_TO_BACKEND_PERMISSIONS
                        .getOrDefault(normalizePermission(permission).toLowerCase(Locale.ROOT), Set.of(normalizePermission(permission)))
                        .stream())
                .filter(permission -> Set.of("MANAGE_USERS", "MANAGE_ROLES", "MANAGE_BANS", "CAN_ORDERS").contains(permission))
                .collect(Collectors.toSet());
    }

    private List<String> toFePermissions(List<String> permissions) {
        return permissions.stream()
                .map(permission -> BACKEND_TO_FE_PERMISSION.getOrDefault(normalizePermission(permission), normalizePermission(permission).toLowerCase(Locale.ROOT)))
                .distinct()
                .toList();
    }

    private String normalizePermission(String permission) {
        return permission == null ? "" : permission.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private record NameParts(String firstName, String lastName) {
    }
}

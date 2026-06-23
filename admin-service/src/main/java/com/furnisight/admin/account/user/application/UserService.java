package com.furnisight.admin.account.user.application;

import com.furnisight.admin.account.infrastructure.grpc.AdminUserGrpcClient;
import com.furnisight.admin.account.role.application.RoleService;
import com.furnisight.admin.account.role.web.dto.response.RoleResponse;
import com.furnisight.admin.account.user.web.dto.request.CreateUserRequest;
import com.furnisight.admin.account.user.web.dto.request.UpdateUserRequest;
import com.furnisight.admin.account.user.web.dto.response.UserResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.PageResponse;
import com.furnisight.admin.user.AccountDetailResponse;
import com.furnisight.admin.user.AccountDto;
import com.furnisight.admin.user.AdminActionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AdminUserGrpcClient userClient;
    private final RoleService roleService;

    public PageResponse<UserResponse> getUsers(int page, int size, String query, String status) {
        com.furnisight.admin.user.AccountPageResponse response =
                userClient.getAccounts(page, size, query, status);
        return toPageResponse(response);
    }

    public PageResponse<UserResponse> getUsers(int page, int size, String query, String status, String scope) {
        com.furnisight.admin.user.AccountPageResponse response =
                userClient.getAccounts(page, size, query, status, scope);
        return toPageResponse(response);
    }

    private PageResponse<UserResponse> toPageResponse(com.furnisight.admin.user.AccountPageResponse response) {
        return new PageResponse<>(
                response.getAccountsList().stream().map(this::toUserResponse).toList(),
                response.getTotalPages(), response.getTotalElements(), response.getCurrentPage());
    }

    public UserResponse getUserById(UUID id) {
        AccountDetailResponse account = userClient.getAccountById(id);
        String firstName = account.getFirstName();
        String lastName = account.getLastName();
        return new UserResponse(
                account.getId(), account.getEmail(), account.getUsername(),
                account.getStatus(),
                account.getCreatedAt(), firstName, lastName, account.getName(), account.getPhone(),
                account.getAvatarUrl(), roleService.toRoleResponses(account.getRolesList()));
    }

    public ActionResultResponse updateUserStatus(UUID adminId, UUID accountId, String status) {
        if ("BANNED".equalsIgnoreCase(status)) {
            return toActionResult(userClient.banAccount(adminId, accountId, "Banned by Admin"));
        }
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return toActionResult(userClient.unbanAccount(adminId, accountId));
        }
        return new ActionResultResponse(false, "Invalid status update");
    }

    public ActionResultResponse updateUser(UUID adminId, UUID accountId, UpdateUserRequest request) {
        boolean changed = false;
        if (request.getName() != null && !request.getName().isBlank()) {
            NameParts nameParts = splitName(request.getName());
            ActionResultResponse profileResult = toActionResult(userClient.updateAccountProfile(
                    accountId, request.getName(), nameParts.firstName(), nameParts.lastName()));
            if (!profileResult.success()) {
                return profileResult;
            }
            changed = true;
        }

        UUID roleId = parseUuid(firstPresent(request.getRoleId(), request.getRole()));
        if (roleId != null) {
            ActionResultResponse roleResult = roleService.assignRole(adminId, accountId, roleId);
            if (!roleResult.success()) {
                return roleResult;
            }
            changed = true;
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            ActionResultResponse statusResult = updateUserStatus(adminId, accountId, request.getStatus());
            if (!statusResult.success()) {
                return statusResult;
            }
            changed = true;
        }

        return changed
                ? new ActionResultResponse(true, "User updated successfully")
                : new ActionResultResponse(false, "No supported user fields to update");
    }

    public ActionResultResponse deleteUser(UUID adminId, UUID accountId) {
        return toActionResult(userClient.deleteAccount(adminId, accountId));
    }

    public ActionResultResponse createUser(UUID adminId, CreateUserRequest request) {
        return toActionResult(userClient.createAccount(
                adminId, request.getEmail(), request.getName(), request.getPhone(),
                request.getPassword(), parseUuid(request.getRole())));
    }

    private UserResponse toUserResponse(AccountDto account) {
        List<RoleResponse> roles = roleService.toRoleResponses(account.getRolesList());
        return new UserResponse(
                account.getId(), account.getEmail(), null, account.getStatus(), account.getCreatedAt(),
                null, null, account.getName(), account.getPhone(), null, roles);
    }

    private ActionResultResponse toActionResult(AdminActionResponse response) {
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
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
        return parts.length == 1 ? new NameParts(parts[0], "") : new NameParts(parts[0], parts[1]);
    }

    private record NameParts(String firstName, String lastName) {
    }
}

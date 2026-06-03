package com.furnisight.admin.integration;

import com.furnisight.admin.user.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GrpcAdminUserClient {

    @GrpcClient("user-service")
    private AdminUserServiceGrpc.AdminUserServiceBlockingStub adminUserServiceStub;

    public AccountPageResponse getAccounts(int page, int size, String query, String status) {
        GetAccountsRequest request = GetAccountsRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setQuery(query == null ? "" : query)
                .setStatus(status == null ? "" : status)
                .build();
        return adminUserServiceStub.getAccounts(request);
    }

    public AccountDetailResponse getAccountById(UUID id) {
        GetAccountByIdRequest request = GetAccountByIdRequest.newBuilder()
                .setId(id.toString())
                .build();
        return adminUserServiceStub.getAccountById(request);
    }

    public RoleListResponse getRoles() {
        return adminUserServiceStub.getRoles(com.google.protobuf.Empty.getDefaultInstance());
    }

    public AdminActionResponse createRole(String name, int position, java.util.List<String> permissions) {
        return adminUserServiceStub.createRole(CreateRoleRequest.newBuilder()
                .setName(name == null ? "" : name)
                .setPosition(position)
                .addAllPermissions(permissions == null ? java.util.List.of() : permissions)
                .build());
    }

    public AdminActionResponse updateRole(String id, String name, int position, java.util.List<String> permissions) {
        return adminUserServiceStub.updateRole(UpdateRoleRequest.newBuilder()
                .setId(id == null ? "" : id)
                .setName(name == null ? "" : name)
                .setPosition(position)
                .addAllPermissions(permissions == null ? java.util.List.of() : permissions)
                .build());
    }

    public AdminActionResponse deleteRole(String id) {
        return adminUserServiceStub.deleteRole(DeleteRoleRequest.newBuilder()
                .setId(id == null ? "" : id)
                .build());
    }

    public AdminActionResponse syncRolePermissions(String roleId, java.util.List<String> permissions) {
        return adminUserServiceStub.syncRolePermissions(SyncRolePermissionsRequest.newBuilder()
                .setRoleId(roleId == null ? "" : roleId)
                .addAllPermissions(permissions == null ? java.util.List.of() : permissions)
                .build());
    }

    public AdminActionResponse banAccount(UUID adminId, UUID accountId, String reason) {
        BanAccountRequest request = BanAccountRequest.newBuilder()
                .setAdminId(adminId.toString())
                .setAccountId(accountId.toString())
                .setReason(reason == null ? "" : reason)
                .build();
        return adminUserServiceStub.banAccount(request);
    }

    public AdminActionResponse unbanAccount(UUID adminId, UUID accountId) {
        UnbanAccountRequest request = UnbanAccountRequest.newBuilder()
                .setAdminId(adminId.toString())
                .setAccountId(accountId.toString())
                .build();
        return adminUserServiceStub.unbanAccount(request);
    }
    
    public AdminActionResponse assignRole(UUID adminId, UUID accountId, UUID roleId) {
        AssignRoleRequest request = AssignRoleRequest.newBuilder()
                .setAdminId(adminId.toString())
                .setAccountId(accountId.toString())
                .setRoleId(roleId.toString())
                .build();
        return adminUserServiceStub.assignRole(request);
    }

    public AdminActionResponse revokeRole(UUID adminId, UUID accountId, UUID roleId) {
        RevokeRoleRequest request = RevokeRoleRequest.newBuilder()
                .setAdminId(adminId.toString())
                .setAccountId(accountId.toString())
                .setRoleId(roleId.toString())
                .build();
        return adminUserServiceStub.revokeRole(request);
    }

    public AdminActionResponse deleteAccount(UUID adminId, UUID accountId) {
        DeleteAccountRequest request = DeleteAccountRequest.newBuilder()
                .setAdminId(adminId.toString())
                .setAccountId(accountId.toString())
                .build();
        return adminUserServiceStub.deleteAccount(request);
    }

    public AdminActionResponse createAccount(UUID adminId, String email, String name, String phone, String password, UUID roleId) {
        CreateAccountRequest request = CreateAccountRequest.newBuilder()
                .setAdminId(adminId.toString())
                .setEmail(email == null ? "" : email)
                .setName(name == null ? "" : name)
                .setPhone(phone == null ? "" : phone)
                .setPassword(password == null ? "" : password)
                .setRoleId(roleId == null ? "" : roleId.toString())
                .build();
        return adminUserServiceStub.createAccount(request);
    }

    public AdminActionResponse updateAccountProfile(UUID accountId, String displayName, String firstName, String lastName) {
        return adminUserServiceStub.updateAccountProfile(UpdateAccountProfileRequest.newBuilder()
                .setAccountId(accountId == null ? "" : accountId.toString())
                .setDisplayName(displayName == null ? "" : displayName)
                .setFirstName(firstName == null ? "" : firstName)
                .setLastName(lastName == null ? "" : lastName)
                .build());
    }

    public AccountStatsResponse getAccountStats() {
        return adminUserServiceStub.getAccountStats(com.google.protobuf.Empty.getDefaultInstance());
    }
}

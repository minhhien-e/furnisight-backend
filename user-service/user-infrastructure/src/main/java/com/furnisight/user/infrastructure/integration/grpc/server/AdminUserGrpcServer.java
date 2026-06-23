package com.furnisight.user.infrastructure.integration.grpc.server;

import com.furnisight.admin.user.*;
import com.furnisight.user.application.account.port.in.usecase.*;
import com.furnisight.user.application.role.port.in.usecase.*;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.enums.identity.AccountStatus;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.repository.profile.UserProfileRepository;
import com.furnisight.user.application.account.dto.*;
import com.furnisight.user.application.account.dto.AssignRoleCommand;
import com.furnisight.user.application.account.dto.RevokeRoleCommand;
import com.furnisight.user.application.role.dto.command.AddRoleCommand;
import com.furnisight.user.application.role.dto.command.AssignPermissionCommand;
import com.furnisight.user.application.role.dto.command.DeleteRoleCommand;
import com.furnisight.user.application.role.dto.command.RevokeRolePermissionCommand;
import com.furnisight.user.application.role.dto.command.UpdateRoleCommand;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.google.protobuf.Empty;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.furnisight.user.infrastructure.database.repository.jpa.identity.AccountJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.furnisight.admin.user.AccountDto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AdminUserGrpcServer extends AdminUserServiceGrpc.AdminUserServiceImplBase {

    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN", "STAFF", "MANAGER", "SUPER_ADMIN");

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final AccountJpaRepository accountJpaRepository;

    private final BanAccountUseCase banAccountUseCase;
    private final UnbanAccountUseCase unbanAccountUseCase;
    private final ActivateAccountUseCase activateAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final CreateAccountUseCase createAccountUseCase;
    private final AssignRoleUseCase assignRoleUseCase;
    private final RevokeRoleUseCase revokeRoleUseCase;
    private final AddRoleUseCase addRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final AssignPermissionUseCase assignPermissionUseCase;
    private final RevokeRolePermissionUseCase revokeRolePermissionUseCase;

    @Override
    public void getAccountStats(Empty request, StreamObserver<AccountStatsResponse> responseObserver) {
        try {
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
            LocalDateTime nextMonthStart = firstDayOfMonth.plusMonths(1).atStartOfDay();

            AccountStatsResponse response = AccountStatsResponse.newBuilder()
                    .setTotalUsers(accountJpaRepository.count())
                    .setActiveUsers(accountJpaRepository.countByStatus(AccountStatus.ACTIVE))
                    .setBannedUsers(accountJpaRepository.countByStatus(AccountStatus.BANNED))
                    .setNewUsersThisMonth(accountJpaRepository.countByCreatedAtBetween(monthStart, nextMonthStart))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting account stats", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAccounts(GetAccountsRequest request, StreamObserver<AccountPageResponse> responseObserver) {
        try {
            int page = request.getPage() > 0 ? request.getPage() - 1 : 0;
            int size = request.getSize() > 0 ? request.getSize() : 10;
            Pageable pageable = PageRequest.of(page, size);

            String query = request.getQuery();
            AccountStatus status = parseStatus(request.getStatus());

            Page<Account> accountPage = switch (request.getScope()) {
                case ACCOUNT_SCOPE_CUSTOMER ->
                        accountJpaRepository.searchCustomerAccounts(query, status, ADMIN_ROLES, pageable);
                case ACCOUNT_SCOPE_ADMIN ->
                        accountJpaRepository.searchAdministrativeAccounts(query, status, ADMIN_ROLES, pageable);
                case ACCOUNT_SCOPE_UNSPECIFIED, UNRECOGNIZED ->
                        accountJpaRepository.searchAccounts(query, status, pageable);
            };

            List<AccountDto> dtoList = accountPage.getContent().stream().map(account -> {
                List<Role> roles = roleRepository.findAllByAccountId(account.getId());
                UserProfile profile = userProfileRepository.findByAccountId(account.getId()).orElse(null);
                List<RoleDto> roleDtos = roles.stream().map(role -> 
                    RoleDto.newBuilder()
                        .setId(role.getId().toString())
                        .setName(role.getName().getValue())
                        .addAllPermissions(role.getPermissions().stream().map(Enum::name).collect(Collectors.toList()))
                        .build()
                ).collect(Collectors.toList());

                return AccountDto.newBuilder()
                        .setId(account.getId().toString())
                        .setUsername(account.getUsername().getValue())
                        .setName(resolveDisplayName(account, profile))
                        .setEmail(account.getEmail().getValue())
                        .setStatus(account.getStatus().name())
                        .setPhone("")
                        .addAllRoles(roleDtos)
                        .setCreatedAt(account.getCreatedAt() == null ? "" : account.getCreatedAt().toString())
                        .build();
            }).collect(Collectors.toList());

            AccountPageResponse response = AccountPageResponse.newBuilder()
                    .addAllAccounts(dtoList)
                    .setTotalPages(accountPage.getTotalPages())
                    .setTotalElements((int) accountPage.getTotalElements())
                    .setCurrentPage(request.getPage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting accounts", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void listMarketingUserIds(ListMarketingUserIdsRequest request,
                                     StreamObserver<MarketingUserIdsResponse> responseObserver) {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            List<Account> candidates = switch (request.getSegment()) {
                case MARKETING_USER_SEGMENT_NEW_USERS ->
                        accountJpaRepository.findAllByStatusAndCreatedAtAfter(AccountStatus.ACTIVE, cutoff);
                case MARKETING_USER_SEGMENT_INACTIVE_30D ->
                        accountJpaRepository.findAllByStatusAndUpdatedAtBefore(AccountStatus.ACTIVE, cutoff);
                case MARKETING_USER_SEGMENT_ALL, UNRECOGNIZED ->
                        accountJpaRepository.findAllByStatus(AccountStatus.ACTIVE);
            };

            List<String> userIds = candidates.stream()
                    .filter(account -> !hasAdministrativeRole(account.getId()))
                    .map(account -> account.getId().toString())
                    .toList();
            responseObserver.onNext(MarketingUserIdsResponse.newBuilder().addAllUserIds(userIds).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error listing marketing user ids", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void batchGetMarketingUsers(BatchGetMarketingUsersRequest request,
                                       StreamObserver<MarketingUsersResponse> responseObserver) {
        try {
            List<UUID> userIds = request.getUserIdsList().stream()
                    .map(this::parseUuid)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            MarketingUsersResponse.Builder response = MarketingUsersResponse.newBuilder();
            accountJpaRepository.findAllById(userIds).forEach(account -> {
                List<Role> roles = roleRepository.findAllByAccountId(account.getId());
                UserProfile profile = userProfileRepository.findByAccountId(account.getId()).orElse(null);
                response.addUsers(MarketingUserDto.newBuilder()
                        .setUserId(account.getId().toString())
                        .setEmail(account.getEmail() == null ? "" : account.getEmail().getValue())
                        .setName(resolveDisplayName(account, profile))
                        .setActive(account.getStatus() == AccountStatus.ACTIVE)
                        .addAllRoles(roles.stream().map(role -> role.getName().getValue()).toList())
                        .build());
            });
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting marketing users", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void filterMarketingUserIds(FilterMarketingUserIdsRequest request,
                                       StreamObserver<FilterMarketingUserIdsResponse> responseObserver) {
        try {
            List<UUID> requestedIds = request.getUserIdsList().stream()
                    .map(this::parseUuid)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            FilterMarketingUserIdsResponse.Builder response = FilterMarketingUserIdsResponse.newBuilder();
            accountJpaRepository.findAllById(requestedIds).forEach(account -> {
                if (hasAdministrativeRole(account.getId())) {
                    response.addAdministrativeUserIds(account.getId().toString());
                } else if (account.getStatus() == AccountStatus.ACTIVE) {
                    response.addEligibleUserIds(account.getId().toString());
                }
            });
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error filtering marketing user ids", e);
            responseObserver.onError(e);
        }
    }

    private boolean hasAdministrativeRole(UUID accountId) {
        return roleRepository.findAllByAccountId(accountId).stream()
                .map(role -> role.getName().getValue())
                .map(value -> value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT))
                .map(value -> value.startsWith("ROLE_") ? value.substring(5) : value)
                .anyMatch(ADMIN_ROLES::contains);
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void getAccountById(GetAccountByIdRequest request, StreamObserver<AccountDetailResponse> responseObserver) {
        try {
            Account account = accountRepository.findById(UUID.fromString(request.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));

            List<Role> roles = roleRepository.findAllByAccountId(account.getId());
            UserProfile profile = userProfileRepository.findByAccountId(account.getId()).orElse(null);

            AccountDetailResponse.Builder builder = AccountDetailResponse.newBuilder()
                    .setId(account.getId().toString())
                    .setEmail(account.getEmail().getValue())
                    .setUsername(account.getUsername().getValue())
                    .setName(resolveDisplayName(account, profile))
                    .setPhone("")
                    .setFirstName(profile == null || profile.getFirstName() == null ? "" : profile.getFirstName())
                    .setLastName(profile == null || profile.getLastName() == null ? "" : profile.getLastName())
                    .setAvatarUrl(profile == null || profile.getAvatarUrl() == null ? "" : profile.getAvatarUrl())
                    .setCreatedAt(account.getCreatedAt() == null ? "" : account.getCreatedAt().toString())
                    .setStatus(account.getStatus().name());

            roles.forEach(role -> {
                builder.addRoles(RoleDto.newBuilder()
                        .setId(role.getId().toString())
                        .setName(role.getName().getValue())
                        .addAllPermissions(role.getPermissions().stream().map(Enum::name).collect(Collectors.toList()))
                        .build());
            });

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting account details", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void banAccount(BanAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            BanAccountCommand command = new BanAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()),
                    request.getReason(),
                    null);
            banAccountUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Banned successfully").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error banning account", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void unbanAccount(UnbanAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            UnbanAccountCommand command = new UnbanAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()));
            unbanAccountUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Unbanned successfully").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error unbanning account", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void activateAccount(ActivateAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            ActivateAccountCommand command = new ActivateAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()));
            activateAccountUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Activated successfully").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error activating account", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            DeleteAccountCommand command = new DeleteAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()));
            deleteAccountUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Deleted successfully").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error deleting account", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getRoles(Empty request, StreamObserver<RoleListResponse> responseObserver) {
        try {
            RoleListResponse response = RoleListResponse.newBuilder()
                    .addAllRoles(roleRepository.findAll().stream()
                            .map(role -> RoleDto.newBuilder()
                                    .setId(role.getId().toString())
                                    .setName(role.getName().getValue())
                                    .addAllPermissions(role.getPermissions().stream().map(Enum::name).toList())
                                    .build())
                            .toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting roles", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void createRole(CreateRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            Role role = addRoleUseCase.execute(new AddRoleCommand(request.getName(), request.getPosition()));
            syncRolePermissions(role.getId(), request.getPermissionsList());
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Role created successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating role", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateRole(UpdateRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            UUID roleId = UUID.fromString(request.getId());
            updateRoleUseCase.execute(new UpdateRoleCommand(roleId, request.getName(), request.getPosition()));
            syncRolePermissions(roleId, request.getPermissionsList());
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Role updated successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error updating role", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteRole(DeleteRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            deleteRoleUseCase.execute(new DeleteRoleCommand(UUID.fromString(request.getId())));
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Role deleted successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error deleting role", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void syncRolePermissions(SyncRolePermissionsRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            syncRolePermissions(UUID.fromString(request.getRoleId()), request.getPermissionsList());
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Role permissions synced successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error syncing role permissions", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void assignRole(AssignRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            AssignRoleCommand command = new AssignRoleCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()),
                    UUID.fromString(request.getRoleId()));
            assignRoleUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Assigned successfully").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error assigning role", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void revokeRole(RevokeRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            RevokeRoleCommand command = new RevokeRoleCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()),
                    UUID.fromString(request.getRoleId()));
            revokeRoleUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Revoked successfully").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error revoking role", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            String password = request.getPassword().isBlank() ? UUID.randomUUID().toString() : request.getPassword();
            UUID roleId = request.getRoleId().isBlank() ? null : UUID.fromString(request.getRoleId());

            CreateAccountCommand command = new CreateAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    request.getEmail(),
                    password,
                    request.getName(),
                    roleId
            );

            createAccountUseCase.execute(command);

            responseObserver.onNext(AdminActionResponse.newBuilder().setSuccess(true)
                    .setMessage("Account created successfully").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating account", e);
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateAccountProfile(UpdateAccountProfileRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        try {
            UUID accountId = UUID.fromString(request.getAccountId());
            UserProfile profile = userProfileRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
            profile.setDisplayName(clean(request.getDisplayName()));
            profile.setFirstName(clean(request.getFirstName()));
            profile.setLastName(clean(request.getLastName()));
            userProfileRepository.save(profile);
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Profile updated successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error updating account profile", e);
            responseObserver.onError(e);
        }
    }

    private AccountStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }
        String status = rawStatus.trim();
        if ("active".equalsIgnoreCase(status)) {
            return AccountStatus.ACTIVE;
        }
        if ("banned".equalsIgnoreCase(status) || "blocked".equalsIgnoreCase(status)) {
            return AccountStatus.BANNED;
        }
        return AccountStatus.valueOf(status.toUpperCase());
    }

    private void syncRolePermissions(UUID roleId, List<String> requestedPermissions) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        Set<String> currentPermissions = role.getPermissions().stream().map(Enum::name).collect(Collectors.toSet());
        Set<String> desiredPermissions = requestedPermissions.stream()
                .map(permission -> permission == null ? "" : permission.trim().toUpperCase())
                .filter(permission -> !permission.isBlank())
                .collect(Collectors.toSet());

        desiredPermissions.stream()
                .filter(permission -> !currentPermissions.contains(permission))
                .forEach(permission -> assignPermissionUseCase.execute(new AssignPermissionCommand(roleId, permission)));

        currentPermissions.stream()
                .filter(permission -> !desiredPermissions.contains(permission))
                .forEach(permission -> revokeRolePermissionUseCase.execute(new RevokeRolePermissionCommand(roleId, permission)));
    }

    private String resolveDisplayName(Account account, UserProfile profile) {
        if (profile != null) {
            if (profile.getDisplayName() != null && !profile.getDisplayName().isBlank()) {
                return profile.getDisplayName();
            }
            String fullName = ((profile.getFirstName() == null ? "" : profile.getFirstName()) + " "
                    + (profile.getLastName() == null ? "" : profile.getLastName())).trim();
            if (!fullName.isBlank()) {
                return fullName;
            }
        }
        return account.getUsername() == null ? "" : account.getUsername().getValue();
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


    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private record NameParts(String firstName, String lastName) {
    }
}

package com.furnisight.user.infrastructure.integration.grpc.server;

import com.furnisight.admin.user.*;
import com.furnisight.user.application.account.port.in.usecase.*;
import com.furnisight.user.application.role.port.in.usecase.*;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.enums.identity.AccountStatus;
import com.furnisight.user.domain.exceptions.DomainException;
import com.furnisight.user.domain.exceptions.identity.AlreadyExistsException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.ForbiddenException;
import com.furnisight.user.domain.exceptions.identity.InvalidOperationException;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.exceptions.identity.UnauthorizedException;
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

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final AccountJpaRepository accountJpaRepository;

    private final BanAccountUseCase banAccountUseCase;
    private final UnbanAccountUseCase unbanAccountUseCase;
    private final ActivateAccountUseCase activateAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final CreateUserAccountByAdminUseCase createUserAccountByAdminUseCase;
    private final CreateAdminAccountUseCase createAdminAccountUseCase;
    private final AssignRoleUseCase assignRoleUseCase;
    private final RevokeRoleUseCase revokeRoleUseCase;
    private final AddRoleUseCase addRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final AssignPermissionUseCase assignPermissionUseCase;
    private final RevokeRolePermissionUseCase revokeRolePermissionUseCase;

    @Override
    public void getAccountStats(Empty request, StreamObserver<AccountStatsResponse> responseObserver) {
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
    }

    @Override
    public void getAccounts(GetAccountsRequest request, StreamObserver<AccountPageResponse> responseObserver) {
            int page = request.getPage() > 0 ? request.getPage() - 1 : 0;
            int size = request.getSize() > 0 ? request.getSize() : 10;
            Pageable pageable = PageRequest.of(page, size);

            String query = request.getQuery();
            AccountStatus status = parseStatus(request.getStatus());

            Page<Account> accountPage;
            if (request.hasIsAdmin()) {
                if (request.getIsAdmin()) {
                    accountPage = accountJpaRepository.searchAdministrativeAccounts(query, status, pageable);
                } else {
                    accountPage = accountJpaRepository.searchCustomerAccounts(query, status, pageable);
                }
            } else {
                accountPage = accountJpaRepository.searchAccounts(query, status, pageable);
            }

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
                        .setUsername("")
                        .setName(resolveDisplayName(account, profile))
                        .setEmail(account.getEmail().getValue())
                        .setStatus(account.getStatus().name())
                        .setPhone(profile == null || profile.getId() == null ? "" : profile.getId().toString())
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
    }

    @Override
    public void listMarketingUserIds(ListMarketingUserIdsRequest request,
                                     StreamObserver<MarketingUserIdsResponse> responseObserver) {
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
                    .filter(account -> !account.isAdmin())
                    .map(account -> account.getId().toString())
                    .toList();
            responseObserver.onNext(MarketingUserIdsResponse.newBuilder().addAllUserIds(userIds).build());
            responseObserver.onCompleted();
    }

    @Override
    public void batchGetMarketingUsers(BatchGetMarketingUsersRequest request,
                                       StreamObserver<MarketingUsersResponse> responseObserver) {
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
    }

    @Override
    public void filterMarketingUserIds(FilterMarketingUserIdsRequest request,
                                       StreamObserver<FilterMarketingUserIdsResponse> responseObserver) {
            List<UUID> requestedIds = request.getUserIdsList().stream()
                    .map(this::parseUuid)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            FilterMarketingUserIdsResponse.Builder response = FilterMarketingUserIdsResponse.newBuilder();
            accountJpaRepository.findAllById(requestedIds).forEach(account -> {
                if (account.isAdmin()) {
                    response.addAdministrativeUserIds(account.getId().toString());
                } else if (account.getStatus() == AccountStatus.ACTIVE) {
                    response.addEligibleUserIds(account.getId().toString());
                }
            });
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
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
            Account account = accountRepository.findById(UUID.fromString(request.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));

            List<Role> roles = roleRepository.findAllByAccountId(account.getId());
            UserProfile profile = userProfileRepository.findByAccountId(account.getId()).orElse(null);

            AccountDetailResponse.Builder builder = AccountDetailResponse.newBuilder()
                    .setId(account.getId().toString())
                    .setEmail(account.getEmail().getValue())
                    .setUsername("")
                    .setName(resolveDisplayName(account, profile))
                    .setPhone(profile == null || profile.getId() == null ? "" : profile.getId().toString())

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
    }

    @Override
    public void getAccountEmailById(GetAccountByIdRequest request, StreamObserver<AccountEmailResponse> responseObserver) {
        try {
            String emailStr = accountRepository.findEmailById(UUID.fromString(request.getId()))
                    .map(com.furnisight.user.domain.valueobjects.identity.Email::getValue)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));

            responseObserver.onNext(AccountEmailResponse.newBuilder().setEmail(emailStr).build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Error fetching account email: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }

    @Override
    public void banAccount(BanAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            BanAccountCommand command = new BanAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()),
                    request.getReason(),
                    null);
            banAccountUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Banned successfully").build());
            responseObserver.onCompleted();
    }

    @Override
    public void unbanAccount(UnbanAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            UnbanAccountCommand command = new UnbanAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()));
            unbanAccountUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Unbanned successfully").build());
            responseObserver.onCompleted();
    }

    @Override
    public void activateAccount(ActivateAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            ActivateAccountCommand command = new ActivateAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()));
            activateAccountUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Activated successfully").build());
            responseObserver.onCompleted();
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            DeleteAccountCommand command = new DeleteAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()));
            deleteAccountUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Deleted successfully").build());
            responseObserver.onCompleted();
    }

    @Override
    public void getRoles(Empty request, StreamObserver<RoleListResponse> responseObserver) {
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
    }

    @Override
    public void createRole(CreateRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            Role role = addRoleUseCase.execute(new AddRoleCommand(request.getName(), request.getPosition()));
            syncRolePermissions(role.getId(), request.getPermissionsList());
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Role created successfully")
                    .build());
            responseObserver.onCompleted();
    }

    @Override
    public void updateRole(UpdateRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            UUID roleId = UUID.fromString(request.getId());
            updateRoleUseCase.execute(new UpdateRoleCommand(roleId, request.getName(), request.getPosition()));
            syncRolePermissions(roleId, request.getPermissionsList());
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Role updated successfully")
                    .build());
            responseObserver.onCompleted();
    }

    @Override
    public void deleteRole(DeleteRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            deleteRoleUseCase.execute(new DeleteRoleCommand(UUID.fromString(request.getId())));
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Role deleted successfully")
                    .build());
            responseObserver.onCompleted();
    }

    @Override
    public void syncRolePermissions(SyncRolePermissionsRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            syncRolePermissions(UUID.fromString(request.getRoleId()), request.getPermissionsList());
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Role permissions synced successfully")
                    .build());
            responseObserver.onCompleted();
    }

    @Override
    public void assignRole(AssignRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            AssignRoleCommand command = new AssignRoleCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()),
                    UUID.fromString(request.getRoleId()));
            assignRoleUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Assigned successfully").build());
            responseObserver.onCompleted();
    }

    @Override
    public void revokeRole(RevokeRoleRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            RevokeRoleCommand command = new RevokeRoleCommand(
                    UUID.fromString(request.getAdminId()),
                    UUID.fromString(request.getAccountId()),
                    UUID.fromString(request.getRoleId()));
            revokeRoleUseCase.execute(command);
            responseObserver.onNext(
                    AdminActionResponse.newBuilder().setSuccess(true).setMessage("Revoked successfully").build());
            responseObserver.onCompleted();
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            String password = request.getPassword().isBlank() ? UUID.randomUUID().toString() : request.getPassword();
            UUID roleId = request.getRoleId().isBlank() ? null : UUID.fromString(request.getRoleId());

            CreateAccountCommand command = new CreateAccountCommand(
                    UUID.fromString(request.getAdminId()),
                    request.getEmail(),
                    password,
                    request.getName(),
                    roleId
            );

            if (request.getIsAdmin()) {
                createAdminAccountUseCase.execute(command);
            } else {
                createUserAccountByAdminUseCase.execute(command);
            }

            responseObserver.onNext(AdminActionResponse.newBuilder().setSuccess(true)
                    .setMessage("Account created successfully").build());
            responseObserver.onCompleted();
    }

    @Override
    public void updateAccountProfile(UpdateAccountProfileRequest request, StreamObserver<AdminActionResponse> responseObserver) {
            UUID accountId = UUID.fromString(request.getAccountId());
            UserProfile profile = userProfileRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
            String displayName = clean(request.getDisplayName());
            String fullName = clean(request.getFirstName()) + " " + clean(request.getLastName());
            fullName = fullName.trim();
            if (fullName.isBlank()) {
                fullName = displayName;
            }

            profile.setDisplayName(displayName);
            profile.setFullName(fullName);
            userProfileRepository.save(profile);
            responseObserver.onNext(AdminActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Profile updated successfully")
                    .build());
            responseObserver.onCompleted();
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
            if (profile.getFullName() != null && !profile.getFullName().isBlank()) {
                return profile.getFullName();
            }
        }
        return account.getEmail() == null ? "" : account.getEmail().getValue();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }



}

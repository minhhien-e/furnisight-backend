package com.furnisight.user.infrastructure.integration.grpc.server;

import com.furnisight.admin.user.FilterMarketingUserIdsRequest;
import com.furnisight.admin.user.FilterMarketingUserIdsResponse;
import com.furnisight.user.application.account.port.in.usecase.ActivateAccountUseCase;
import com.furnisight.user.application.account.port.in.usecase.AssignRoleUseCase;
import com.furnisight.user.application.account.port.in.usecase.BanAccountUseCase;
import com.furnisight.user.application.account.port.in.usecase.CreateAccountUseCase;
import com.furnisight.user.application.account.port.in.usecase.DeleteAccountUseCase;
import com.furnisight.user.application.account.port.in.usecase.RevokeRoleUseCase;
import com.furnisight.user.application.account.port.in.usecase.UnbanAccountUseCase;
import com.furnisight.user.application.role.port.in.usecase.AddRoleUseCase;
import com.furnisight.user.application.role.port.in.usecase.AssignPermissionUseCase;
import com.furnisight.user.application.role.port.in.usecase.DeleteRoleUseCase;
import com.furnisight.user.application.role.port.in.usecase.RevokeRolePermissionUseCase;
import com.furnisight.user.application.role.port.in.usecase.UpdateRoleUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.enums.identity.AccountStatus;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.repository.profile.UserProfileRepository;
import com.furnisight.user.domain.valueobjects.identity.RoleName;
import com.furnisight.user.infrastructure.database.repository.jpa.identity.AccountJpaRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserGrpcServerMarketingTest {
    @Mock AccountRepository accountRepository;
    @Mock RoleRepository roleRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock AccountJpaRepository accountJpaRepository;
    @Mock BanAccountUseCase banAccountUseCase;
    @Mock UnbanAccountUseCase unbanAccountUseCase;
    @Mock ActivateAccountUseCase activateAccountUseCase;
    @Mock DeleteAccountUseCase deleteAccountUseCase;
    @Mock CreateAccountUseCase createAccountUseCase;
    @Mock AssignRoleUseCase assignRoleUseCase;
    @Mock RevokeRoleUseCase revokeRoleUseCase;
    @Mock AddRoleUseCase addRoleUseCase;
    @Mock UpdateRoleUseCase updateRoleUseCase;
    @Mock DeleteRoleUseCase deleteRoleUseCase;
    @Mock AssignPermissionUseCase assignPermissionUseCase;
    @Mock RevokeRolePermissionUseCase revokeRolePermissionUseCase;
    @Mock StreamObserver<FilterMarketingUserIdsResponse> observer;
    @InjectMocks AdminUserGrpcServer server;

    @Test
    void reportsAdministrativeAccountsInsteadOfMakingThemEligible() {
        UUID userId = UUID.randomUUID();
        Account account = new Account();
        account.setId(userId);
        account.setStatus(AccountStatus.ACTIVE);
        when(accountJpaRepository.findAllById(List.of(userId))).thenReturn(List.of(account));
        when(roleRepository.findAllByAccountId(userId)).thenReturn(List.of(new Role(new RoleName("ADMIN"), 1)));

        server.filterMarketingUserIds(FilterMarketingUserIdsRequest.newBuilder()
                .addUserIds(userId.toString()).build(), observer);

        ArgumentCaptor<FilterMarketingUserIdsResponse> response =
                ArgumentCaptor.forClass(FilterMarketingUserIdsResponse.class);
        verify(observer).onNext(response.capture());
        verify(observer).onCompleted();
        assertThat(response.getValue().getEligibleUserIdsList()).isEmpty();
        assertThat(response.getValue().getAdministrativeUserIdsList()).containsExactly(userId.toString());
    }
}

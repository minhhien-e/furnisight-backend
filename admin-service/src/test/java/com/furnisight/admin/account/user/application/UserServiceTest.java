package com.furnisight.admin.account.user.application;

import com.furnisight.admin.account.infrastructure.grpc.AdminUserGrpcClient;
import com.furnisight.admin.account.role.application.RoleService;
import com.furnisight.admin.account.role.web.dto.response.RoleResponse;
import com.furnisight.admin.user.AccountDto;
import com.furnisight.admin.user.AccountPageResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void copiesUserFieldsWithoutPresentationMapping() {
        AdminUserGrpcClient client = mock(AdminUserGrpcClient.class);
        RoleService roleService = mock(RoleService.class);
        when(client.getAccounts(1, 10, "minh", "ACTIVE"))
                .thenReturn(AccountPageResponse.newBuilder()
                        .addAccounts(AccountDto.newBuilder()
                                .setId("user-1")
                                .setName("Minh")
                                .setEmail("minh@example.com")
                                .setStatus("ACTIVE")
                                .setCreatedAt("2026-06-10T10:00:00")
                                .build())
                        .setCurrentPage(1)
                        .setTotalPages(1)
                        .setTotalElements(1)
                        .build());
        when(roleService.toRoleResponses(anyList()))
                .thenReturn(List.of(new RoleResponse("role-1", "Admin", List.of("user_view"))));

        var response = new UserService(client, roleService)
                .getUsers(1, 10, "minh", "ACTIVE");

        assertThat(response.items()).singleElement().satisfies(user -> {
            assertThat(user.status()).isEqualTo("ACTIVE");
            assertThat(user.roles()).containsExactly(new RoleResponse("role-1", "Admin", List.of("user_view")));
            assertThat(user.createdAt()).isEqualTo("2026-06-10T10:00:00");
        });
    }
}

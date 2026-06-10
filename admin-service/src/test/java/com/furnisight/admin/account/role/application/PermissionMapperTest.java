package com.furnisight.admin.account.role.application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionMapperTest {

    private final PermissionMapper mapper = new PermissionMapper();

    @Test
    void preservesExistingFrontendBackendMapping() {
        assertThat(mapper.toBackendPermissions(List.of("user_view", "order_update")))
                .containsExactlyInAnyOrder("MANAGE_USERS", "CAN_ORDERS");
        assertThat(mapper.toFrontendPermissions(List.of("MANAGE_USERS", "CAN_ORDERS")))
                .containsExactly("user_view", "order_view");
    }
}

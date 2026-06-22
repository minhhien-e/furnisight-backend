package com.furnisight.admin.account.role.application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionMapperTest {

    private final PermissionMapper mapper = new PermissionMapper();

    @Test
    void mapsCurrentFrontendBackendPermissions() {
        assertThat(mapper.toBackendPermissions(List.of("product_manage", "order_manage")))
                .containsExactlyInAnyOrder("PRODUCT_MANAGE", "ORDER_MANAGE");
        assertThat(mapper.toFrontendPermissions(List.of("PRODUCT_MANAGE", "ORDER_MANAGE")))
                .containsExactly("order_manage", "product_manage");
    }
}

package com.furnisight.order.adapter.out.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEmailGrpcClientTest {

    @Test
    void nullUserIdFailsInsteadOfReturningEmptyEmail() {
        UserEmailGrpcClient client = new UserEmailGrpcClient();

        assertThatThrownBy(() -> client.getEmailByUserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId cannot be null");
    }
}

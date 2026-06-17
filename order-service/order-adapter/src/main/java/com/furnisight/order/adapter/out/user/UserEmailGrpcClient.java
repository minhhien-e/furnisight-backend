package com.furnisight.order.adapter.out.user;

import com.furnisight.admin.user.AdminUserServiceGrpc;
import com.furnisight.admin.user.GetAccountByIdRequest;
import com.furnisight.admin.user.AccountDetailResponse;
import com.furnisight.order.application.user.port.out.UserEmailPort;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserEmailGrpcClient implements UserEmailPort {

    @GrpcClient("user-service")
    private AdminUserServiceGrpc.AdminUserServiceBlockingStub adminUserStub;

    @Override
    public String getEmailByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        GetAccountByIdRequest request = GetAccountByIdRequest.newBuilder()
                .setId(userId.toString())
                .build();
        AccountDetailResponse response = adminUserStub.getAccountById(request);
        return response.getEmail();
    }
}

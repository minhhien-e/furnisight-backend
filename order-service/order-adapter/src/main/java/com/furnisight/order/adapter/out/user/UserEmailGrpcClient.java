package com.furnisight.order.adapter.out.user;

import com.furnisight.admin.user.AdminUserServiceGrpc;
import com.furnisight.admin.user.GetAccountByIdRequest;
import com.furnisight.admin.user.AccountDetailResponse;
import com.furnisight.order.application.user.port.out.UserEmailPort;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class UserEmailGrpcClient implements UserEmailPort {

    @GrpcClient("user-service")
    private AdminUserServiceGrpc.AdminUserServiceBlockingStub adminUserStub;

    @Override
    public String getEmailByUserId(UUID userId) {
        if (userId == null) {
            return "";
        }
        try {
            GetAccountByIdRequest request = GetAccountByIdRequest.newBuilder()
                    .setId(userId.toString())
                    .build();
            AccountDetailResponse response = adminUserStub.getAccountById(request);
            return response.getEmail();
        } catch (Exception e) {
            log.error("Failed to fetch email for userId {} from user-service: {}", userId, e.getMessage());
            return "";
        }
    }
}

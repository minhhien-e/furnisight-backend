package com.furnisight.notification.adapter.in.grpc;

import com.furnisight.notification.CreateNotificationProfileRequest;
import com.furnisight.notification.CreateNotificationProfileResponse;
import com.furnisight.notification.NotificationServiceGrpc;
import com.furnisight.notification.application.profile.port.in.command.CreateDefaultNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.usecase.CreateDefaultNotificationProfileUseCase;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class GrpcNotificationProfileService extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final CreateDefaultNotificationProfileUseCase createDefaultNotificationProfileUseCase;

    @Override
    public void createNotificationProfile(CreateNotificationProfileRequest request, StreamObserver<CreateNotificationProfileResponse> responseObserver) {
        try {
            CreateDefaultNotificationProfileCommand command = new CreateDefaultNotificationProfileCommand(
                    UUID.fromString(request.getAccountId())
            );
            createDefaultNotificationProfileUseCase.execute(command);

            CreateNotificationProfileResponse response = CreateNotificationProfileResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Notification profile created successfully")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}

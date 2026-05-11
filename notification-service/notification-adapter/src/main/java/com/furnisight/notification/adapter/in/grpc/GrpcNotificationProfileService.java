package com.furnisight.notification.adapter.in.grpc;

import com.furnisight.notification.CreateNotificationProfileRequest;
import com.furnisight.notification.CreateNotificationProfileResponse;
import com.furnisight.notification.DeleteNotificationProfileRequest;
import com.furnisight.notification.DeleteNotificationProfileResponse;
import com.furnisight.notification.NotificationServiceGrpc;
import com.furnisight.notification.UpdateNotificationProfileRequest;
import com.furnisight.notification.UpdateNotificationProfileResponse;
import com.furnisight.notification.application.profile.port.in.command.CreateDefaultNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.command.DeleteNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.command.UpdateNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.usecase.CreateDefaultNotificationProfileUseCase;
import com.furnisight.notification.application.profile.port.in.usecase.DeleteNotificationProfileUseCase;
import com.furnisight.notification.application.profile.port.in.usecase.UpdateNotificationProfileUseCase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class GrpcNotificationProfileService extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final CreateDefaultNotificationProfileUseCase createDefaultNotificationProfileUseCase;
    private final UpdateNotificationProfileUseCase updateNotificationProfileUseCase;
    private final DeleteNotificationProfileUseCase deleteNotificationProfileUseCase;

    @Override
    public void createNotificationProfile(CreateNotificationProfileRequest request, StreamObserver<CreateNotificationProfileResponse> responseObserver) {
        try {
            CreateDefaultNotificationProfileCommand command = new CreateDefaultNotificationProfileCommand(
                    UUID.fromString(request.getAccountId()),
                    request.getEmail()
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

    @Override
    public void updateNotificationProfile(UpdateNotificationProfileRequest request, StreamObserver<UpdateNotificationProfileResponse> responseObserver) {
        try {
            UpdateNotificationProfileCommand command = UpdateNotificationProfileCommand.builder()
                    .userId(UUID.fromString(request.getAccountId()))
                    .email(request.getEmail())
                    .build();
            updateNotificationProfileUseCase.execute(command);

            UpdateNotificationProfileResponse response = UpdateNotificationProfileResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Notification profile updated successfully")
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

    @Override
    public void deleteNotificationProfile(DeleteNotificationProfileRequest request, StreamObserver<DeleteNotificationProfileResponse> responseObserver) {
        try {
            DeleteNotificationProfileCommand command = DeleteNotificationProfileCommand.builder()
                    .userId(UUID.fromString(request.getAccountId()))
                    .build();
            deleteNotificationProfileUseCase.execute(command);

            DeleteNotificationProfileResponse response = DeleteNotificationProfileResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Notification profile deleted successfully")
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

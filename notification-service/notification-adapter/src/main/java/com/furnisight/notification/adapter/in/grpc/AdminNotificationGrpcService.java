package com.furnisight.notification.adapter.in.grpc;

import com.furnisight.admin.notification.AdminActionResponse;
import com.furnisight.admin.notification.AdminNotificationServiceGrpc;
import com.furnisight.admin.notification.CreateTemplateRequest;
import com.furnisight.admin.notification.DeleteTemplateRequest;
import com.furnisight.admin.notification.GetTemplateByCodeRequest;
import com.furnisight.admin.notification.GetTemplatesRequest;
import com.furnisight.admin.notification.NotificationTemplateListResponse;
import com.furnisight.admin.notification.NotificationTemplateResponse;
import com.furnisight.admin.notification.UpdateTemplateRequest;
import com.furnisight.notification.application.template.port.in.dto.command.CreateNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.dto.command.UpdateNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.dto.query.FilterNotificationTemplateQuery;
import com.furnisight.notification.application.template.port.in.usecase.CreateNotificationTemplateUseCase;
import com.furnisight.notification.application.template.port.in.usecase.DeleteNotificationTemplateUseCase;
import com.furnisight.notification.application.template.port.in.usecase.FilterNotificationTemplateUseCase;
import com.furnisight.notification.application.template.port.in.usecase.GetNotificationTemplateUseCase;
import com.furnisight.notification.application.template.port.in.usecase.UpdateNotificationTemplateUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class AdminNotificationGrpcService extends AdminNotificationServiceGrpc.AdminNotificationServiceImplBase {

    private final FilterNotificationTemplateUseCase filterUseCase;
    private final GetNotificationTemplateUseCase getUseCase;
    private final CreateNotificationTemplateUseCase createUseCase;
    private final UpdateNotificationTemplateUseCase updateUseCase;
    private final DeleteNotificationTemplateUseCase deleteUseCase;

    @Override
    public void getNotificationTemplates(GetTemplatesRequest request, StreamObserver<NotificationTemplateListResponse> responseObserver) {
        FilterNotificationTemplateQuery query = FilterNotificationTemplateQuery.builder()
                .name(request.hasName() ? request.getName() : null)
                .channel(request.hasChannel() ? NotificationChannel.valueOf(request.getChannel()) : null)
                .type(request.hasType() ? NotificationType.valueOf(request.getType()) : null)
                .build();

        List<NotificationTemplateResponse> responseList = filterUseCase.execute(query).stream()
                .map(this::mapToProtoResponse)
                .collect(Collectors.toList());

        NotificationTemplateListResponse response = NotificationTemplateListResponse.newBuilder()
                .addAllItems(responseList)
                .build();

        complete(responseObserver, response);
    }

    @Override
    public void getNotificationTemplateByCode(GetTemplateByCodeRequest request, StreamObserver<NotificationTemplateResponse> responseObserver) {
        var result = getUseCase.execute(request.getCode());
        complete(responseObserver, mapToProtoResponse(result));
    }

    @Override
    public void createNotificationTemplate(CreateTemplateRequest request, StreamObserver<NotificationTemplateResponse> responseObserver) {
        CreateNotificationTemplateCommand command = CreateNotificationTemplateCommand.builder()
                .code(request.getCode())
                .name(request.getName())
                .titleTemplate(request.getTitleTemplate())
                .bodyTemplate(request.getBodyTemplate())
                .type(NotificationType.valueOf(request.getType()))
                .channel(NotificationChannel.valueOf(request.getChannel()))
                .defaultImage(request.hasDefaultImage() ? request.getDefaultImage() : null)
                .defaultActionUrl(request.hasDefaultActionUrl() ? request.getDefaultActionUrl() : null)
                .build();

        var result = createUseCase.execute(command);
        complete(responseObserver, mapToProtoResponse(result));
    }

    @Override
    public void updateNotificationTemplate(UpdateTemplateRequest request, StreamObserver<NotificationTemplateResponse> responseObserver) {
        UpdateNotificationTemplateCommand command = UpdateNotificationTemplateCommand.builder()
                .id(request.getId())
                .name(request.getName())
                .titleTemplate(request.getTitleTemplate())
                .bodyTemplate(request.getBodyTemplate())
                .build();

        var result = updateUseCase.execute(command);
        complete(responseObserver, mapToProtoResponse(result));
    }

    @Override
    public void deleteNotificationTemplate(DeleteTemplateRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        deleteUseCase.execute(request.getId());
        complete(responseObserver, AdminActionResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Notification template deleted")
                .build());
    }

    private NotificationTemplateResponse mapToProtoResponse(com.furnisight.notification.application.template.port.in.dto.response.NotificationTemplateResponse response) {
        NotificationTemplateResponse.Builder builder = NotificationTemplateResponse.newBuilder()
                .setId(response.getId())
                .setCode(response.getCode())
                .setName(response.getName())
                .setTitleTemplate(response.getTitleTemplate())
                .setBodyTemplate(response.getBodyTemplate())
                .setType(response.getType().name())
                .setChannel(response.getChannel().name());

        if (response.getVariables() != null) {
            builder.addAllVariables(response.getVariables());
        }
        if (response.getDefaultImage() != null) {
            builder.setDefaultImage(response.getDefaultImage());
        }
        if (response.getDefaultActionUrl() != null) {
            builder.setDefaultActionUrl(response.getDefaultActionUrl());
        }
        return builder.build();
    }

    private <T> void complete(StreamObserver<T> observer, T value) {
        observer.onNext(value);
        observer.onCompleted();
    }
}

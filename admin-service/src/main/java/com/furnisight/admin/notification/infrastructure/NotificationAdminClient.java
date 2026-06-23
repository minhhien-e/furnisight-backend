package com.furnisight.admin.notification.infrastructure;

import com.furnisight.admin.notification.AdminNotificationServiceGrpc;
import com.furnisight.admin.notification.GetTemplateByCodeRequest;
import com.furnisight.admin.notification.GetTemplatesRequest;
import com.furnisight.admin.notification.DeleteTemplateRequest;
import com.furnisight.admin.notification.web.dto.CreateNotificationTemplateRequest;
import com.furnisight.admin.notification.web.dto.NotificationChannel;
import com.furnisight.admin.notification.web.dto.NotificationTemplateResponse;
import com.furnisight.admin.notification.web.dto.NotificationType;
import com.furnisight.admin.notification.web.dto.UpdateNotificationTemplateRequest;
import com.furnisight.admin.shared.web.ActionResultResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NotificationAdminClient {

    @GrpcClient("notification-service")
    private AdminNotificationServiceGrpc.AdminNotificationServiceBlockingStub notificationStub;

    public List<NotificationTemplateResponse> getTemplates(String name, String channel, String type) {
        GetTemplatesRequest.Builder requestBuilder = GetTemplatesRequest.newBuilder();
        if (name != null) requestBuilder.setName(name);
        if (channel != null) requestBuilder.setChannel(channel);
        if (type != null) requestBuilder.setType(type);

        var response = notificationStub.getNotificationTemplates(requestBuilder.build());
        return response.getItemsList().stream()
                .map(this::mapFromProto)
                .collect(Collectors.toList());
    }

    public NotificationTemplateResponse getTemplateByCode(String code) {
        var response = notificationStub.getNotificationTemplateByCode(GetTemplateByCodeRequest.newBuilder().setCode(code).build());
        return mapFromProto(response);
    }

    public NotificationTemplateResponse createTemplate(CreateNotificationTemplateRequest request) {
        com.furnisight.admin.notification.CreateTemplateRequest.Builder builder = com.furnisight.admin.notification.CreateTemplateRequest.newBuilder()
                .setCode(request.getCode())
                .setName(request.getName())
                .setTitleTemplate(request.getTitleTemplate())
                .setBodyTemplate(request.getBodyTemplate())
                .setType(request.getType().name())
                .setChannel(request.getChannel().name());
        
        if (request.getDefaultImage() != null) builder.setDefaultImage(request.getDefaultImage());
        if (request.getDefaultActionUrl() != null) builder.setDefaultActionUrl(request.getDefaultActionUrl());

        var response = notificationStub.createNotificationTemplate(builder.build());
        return mapFromProto(response);
    }

    public NotificationTemplateResponse updateTemplate(UUID templateId, UpdateNotificationTemplateRequest request) {
        var response = notificationStub.updateNotificationTemplate(com.furnisight.admin.notification.UpdateTemplateRequest.newBuilder()
                .setId(templateId.toString())
                .setName(request.getName())
                .setTitleTemplate(request.getTitleTemplate())
                .setBodyTemplate(request.getBodyTemplate())
                .build());
        return mapFromProto(response);
    }

    public ActionResultResponse deleteTemplate(UUID templateId) {
        var response = notificationStub.deleteNotificationTemplate(DeleteTemplateRequest.newBuilder().setId(templateId.toString()).build());
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private NotificationTemplateResponse mapFromProto(com.furnisight.admin.notification.NotificationTemplateResponse proto) {
        return new NotificationTemplateResponse(
                UUID.fromString(proto.getId()),
                proto.getCode(),
                proto.getName(),
                proto.getVariablesList(),
                proto.getTitleTemplate(),
                proto.getBodyTemplate(),
                NotificationType.valueOf(proto.getType()),
                NotificationChannel.valueOf(proto.getChannel()),
                proto.hasDefaultImage() ? proto.getDefaultImage() : null,
                proto.hasDefaultActionUrl() ? proto.getDefaultActionUrl() : null
        );
    }
}

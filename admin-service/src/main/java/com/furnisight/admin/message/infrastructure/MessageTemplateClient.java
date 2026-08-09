package com.furnisight.admin.message.infrastructure;

import com.furnisight.admin.message.grpc.*;
import com.furnisight.admin.message.web.dto.request.UpsertMessageTemplateRequest;
import com.furnisight.admin.message.web.dto.response.MessageTemplateResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageTemplateClient {

    @GrpcClient("message-service")
    private AdminMessageServiceGrpc.AdminMessageServiceBlockingStub messageServiceStub;

    public List<MessageTemplateResponse> getTemplates() {
        try {
            GetMessageTemplatesResponse response = messageServiceStub.getTemplates(EmptyMessageTemplateRequest.newBuilder().build());
            return response.getTemplatesList().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get message templates via gRPC", e);
            return Collections.emptyList();
        }
    }

    public MessageTemplateResponse getTemplateById(Integer id) {
        try {
            MessageTemplateDto response = messageServiceStub.getTemplateById(
                    GetMessageTemplateByIdRequest.newBuilder().setId(id).build());
            return toResponse(response);
        } catch (Exception e) {
            log.error("Failed to get message template by id={} via gRPC", id, e);
            throw new RuntimeException("Không thể lấy thông tin mẫu tin nhắn");
        }
    }

    public MessageTemplateResponse createTemplate(UpsertMessageTemplateRequest request) {
        try {
            MessageTemplateDto response = messageServiceStub.createTemplate(
                    com.furnisight.admin.message.grpc.UpsertMessageTemplateRequest.newBuilder()
                            .setTitle(request.getTitle() != null ? request.getTitle() : "")
                            .setContent(request.getContent() != null ? request.getContent() : "")
                            .setCategory(request.getCategory() != null ? request.getCategory() : "")
                            .setActive(request.getActive() != null ? request.getActive() : false)
                            .build()
            );
            return toResponse(response);
        } catch (Exception e) {
            log.error("Failed to create message template via gRPC", e);
            throw new RuntimeException("Không thể tạo mẫu tin nhắn: " + e.getMessage());
        }
    }

    public MessageTemplateResponse updateTemplate(Integer id, UpsertMessageTemplateRequest request) {
        try {
            MessageTemplateDto response = messageServiceStub.updateTemplate(
                    UpdateMessageTemplateRequest.newBuilder()
                            .setId(id)
                            .setData(
                                    com.furnisight.admin.message.grpc.UpsertMessageTemplateRequest.newBuilder()
                                            .setTitle(request.getTitle() != null ? request.getTitle() : "")
                                            .setContent(request.getContent() != null ? request.getContent() : "")
                                            .setCategory(request.getCategory() != null ? request.getCategory() : "")
                                            .setActive(request.getActive() != null ? request.getActive() : false)
                                            .build()
                            )
                            .build()
            );
            return toResponse(response);
        } catch (Exception e) {
            log.error("Failed to update message template id={} via gRPC", id, e);
            throw new RuntimeException("Không thể cập nhật mẫu tin nhắn: " + e.getMessage());
        }
    }

    public ActionResultResponse deleteTemplate(Integer id) {
        try {
            DeleteMessageTemplateResponse response = messageServiceStub.deleteTemplate(
                    DeleteMessageTemplateRequest.newBuilder().setId(id).build());
            return new ActionResultResponse(response.getSuccess(), response.getMessage());
        } catch (Exception e) {
            log.error("Failed to delete message template id={} via gRPC", id, e);
            return new ActionResultResponse(false, "Không thể xóa mẫu tin nhắn: " + e.getMessage());
        }
    }

    private MessageTemplateResponse toResponse(MessageTemplateDto dto) {
        return MessageTemplateResponse.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .active(dto.getActive())
                .createdAt(parseDateTime(dto.getCreatedAt()))
                .updatedAt(parseDateTime(dto.getUpdatedAt()))
                .build();
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

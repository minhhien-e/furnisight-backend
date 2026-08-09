package com.furnisight.message.grpc.server;

import com.furnisight.admin.message.grpc.*;
import com.furnisight.message.database.entity.MessageTemplate;
import com.furnisight.message.database.repository.MessageTemplateRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AdminMessageTemplateGrpcServer extends AdminMessageServiceGrpc.AdminMessageServiceImplBase {

    private final MessageTemplateRepository templateRepository;

    @Override
    public void getTemplates(EmptyMessageTemplateRequest request, StreamObserver<GetMessageTemplatesResponse> responseObserver) {
        try {
            List<MessageTemplate> templates = templateRepository.findAll();
            GetMessageTemplatesResponse response = GetMessageTemplatesResponse.newBuilder()
                    .addAllTemplates(templates.stream().map(this::toDto).toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get templates", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getTemplateById(GetMessageTemplateByIdRequest request, StreamObserver<MessageTemplateDto> responseObserver) {
        try {
            MessageTemplate template = templateRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));
            responseObserver.onNext(toDto(template));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get template", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void createTemplate(UpsertMessageTemplateRequest request, StreamObserver<MessageTemplateDto> responseObserver) {
        try {
            MessageTemplate template = MessageTemplate.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .category(request.getCategory() != null && !request.getCategory().isEmpty() ? request.getCategory() : "GREETING")
                    .active(request.getActive())
                    .build();
            MessageTemplate saved = templateRepository.save(template);
            responseObserver.onNext(toDto(saved));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to create template", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void updateTemplate(UpdateMessageTemplateRequest request, StreamObserver<MessageTemplateDto> responseObserver) {
        try {
            MessageTemplate template = templateRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));

            UpsertMessageTemplateRequest data = request.getData();
            if (data.getTitle() != null && !data.getTitle().isEmpty()) template.setTitle(data.getTitle());
            if (data.getContent() != null && !data.getContent().isEmpty()) template.setContent(data.getContent());
            if (data.getCategory() != null && !data.getCategory().isEmpty()) template.setCategory(data.getCategory());
            template.setActive(data.getActive());

            MessageTemplate updated = templateRepository.save(template);
            responseObserver.onNext(toDto(updated));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to update template", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void deleteTemplate(DeleteMessageTemplateRequest request, StreamObserver<DeleteMessageTemplateResponse> responseObserver) {
        try {
            MessageTemplate template = templateRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));
            templateRepository.delete(template);
            responseObserver.onNext(DeleteMessageTemplateResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to delete template", ex);
            responseObserver.onError(ex);
        }
    }

    private MessageTemplateDto toDto(MessageTemplate entity) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        MessageTemplateDto.Builder builder = MessageTemplateDto.newBuilder()
                .setId(entity.getId())
                .setTitle(entity.getTitle() != null ? entity.getTitle() : "")
                .setContent(entity.getContent() != null ? entity.getContent() : "")
                .setCategory(entity.getCategory() != null ? entity.getCategory() : "")
                .setActive(entity.getActive() != null ? entity.getActive() : false);
        
        if (entity.getCreatedAt() != null) {
            builder.setCreatedAt(entity.getCreatedAt().format(formatter));
        }
        if (entity.getUpdatedAt() != null) {
            builder.setUpdatedAt(entity.getUpdatedAt().format(formatter));
        }
        
        return builder.build();
    }
}

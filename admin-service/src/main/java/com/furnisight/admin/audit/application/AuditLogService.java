package com.furnisight.admin.audit.application;

import com.furnisight.admin.account.infrastructure.grpc.AdminUserGrpcClient;
import com.furnisight.admin.user.AccountDetailResponse;
import com.furnisight.admin.user.AccountDto;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.audit.web.dto.response.AuditLogResponse;
import com.furnisight.admin.audit.infrastructure.persistence.AuditLog;
import com.furnisight.admin.shared.web.PageResponse;
import com.furnisight.admin.audit.infrastructure.persistence.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AuditLogRepository repository;
    private final AdminUserGrpcClient userClient;

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getLogs(String search, String type, String result, java.time.LocalDateTime fromDate, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> logs = repository.findAll(
                com.furnisight.admin.audit.infrastructure.persistence.AuditLogSpecifications.buildSpec(search, type, result, fromDate), pageable);
        return new PageResponse<>(
                logs.getContent().stream().map(this::toResponse).toList(),
                logs.getTotalPages(),
                logs.getTotalElements(),
                safePage,
                safeSize);
    }



    /**
     * Hàm tiện ích (Convenience wrapper) 1: 
     * Hỗ trợ tự động bóc tách trạng thái thành công/thất bại từ ActionResultResponse.
     */
    @Transactional
    public void record(UUID actorId,
                       String actionType,
                       String action,
                       String resourceType,
                       String resourceId,
                       ActionResultResponse result,
                       String detail,
                       HttpServletRequest request) {
        record(actorId, actionType, action, resourceType, resourceId, result != null && result.success(), detail, request);
    }

    /**
     * Hàm tiện ích (Convenience wrapper) 2: 
     * Hỗ trợ tự động nội suy (extract) các chuỗi cấu hình (type, description, resourceType) từ enum AuditAction.
     */
    @Transactional
    public void record(UUID actorId,
                       com.furnisight.admin.audit.domain.AuditAction action,
                       String resourceId,
                       ActionResultResponse result,
                       String detail,
                       HttpServletRequest request) {
        record(actorId, action.getType(), action.getDescription(), action.getResourceType(), 
               resourceId, result != null && result.success(), detail, request);
    }



    /**
     * Hàm lõi (Core method):
     * Nhận dữ liệu thô và thực hiện ghi trực tiếp xuống Database.
     * Hàm này cũng đảm nhiệm việc fetch actorName bằng gRPC ngay lúc ghi để tối ưu tốc độ đọc (read-path).
     */
    @Transactional
    public void record(UUID actorId,
                       String actionType,
                       String action,
                       String resourceType,
                       String resourceId,
                       boolean success,
                       String detail,
                       HttpServletRequest request) {
        try {
            AuditLog logEntry = new AuditLog();
            logEntry.setActorId(actorId);

            if (actorId != null) {
                try {
                    AccountDetailResponse account = userClient.getAccountById(actorId);
                    String name = account.getName() != null && !account.getName().isBlank() ? account.getName() : 
                               (account.getUsername() != null && !account.getUsername().isBlank() ? account.getUsername() : account.getEmail());
                    logEntry.setActorName(name);
                } catch (Exception ex) {
                    log.debug("Failed to fetch actor info during audit log", ex);
                }
            }

            logEntry.setActionType(actionType);
            logEntry.setAction(action);
            logEntry.setResourceType(resourceType);
            logEntry.setResourceId(resourceId);
            logEntry.setResult(success ? "success" : "error");
            logEntry.setDetail(detail);
            logEntry.setIpAddress(request != null ? request.getRemoteAddr() : null);
            logEntry.setUserAgent(request != null ? request.getHeader("User-Agent") : null);
            repository.save(logEntry);
        } catch (Exception ex) {
            log.warn("Failed to write admin audit log", ex);
        }
    }

    private AuditLogResponse toResponse(AuditLog logEntry) {
        return new AuditLogResponse(
                logEntry.getId().toString(),
                logEntry.getActorId() == null ? null : logEntry.getActorId().toString(),
                logEntry.getActorName(),
                logEntry.getActionType(),
                logEntry.getAction(),
                logEntry.getResourceType(),
                logEntry.getResourceId(),
                logEntry.getResult(),
                logEntry.getDetail(),
                logEntry.getIpAddress(),
                logEntry.getUserAgent(),
                logEntry.getCreatedAt() == null ? null : logEntry.getCreatedAt().toString());
    }





}

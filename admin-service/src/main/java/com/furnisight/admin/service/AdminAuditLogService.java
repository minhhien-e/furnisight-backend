package com.furnisight.admin.service;

import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminAuditLogPageResponse;
import com.furnisight.admin.controller.dto.AdminAuditLogResponse;
import com.furnisight.admin.entity.AdminAuditLog;
import com.furnisight.admin.repository.AdminAuditLogRepository;
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
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AdminAuditLogRepository repository;

    @Transactional(readOnly = true)
    public AdminAuditLogPageResponse getLogs(String search, String type, String result, String period, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminAuditLog> logs = repository.findAll(buildSpec(search, type, result, period), pageable);

        return new AdminAuditLogPageResponse(
                logs.getContent().stream().map(this::toResponse).toList(),
                logs.getTotalElements(),
                safePage,
                safeSize,
                logs.getTotalPages());
    }

    private Specification<AdminAuditLog> buildSpec(String search, String type, String result, String period) {
        return Specification
                .where(matchesSearch(search))
                .and(equalsField("actionType", normalizeFilter(type)))
                .and(equalsField("result", normalizeFilter(result)))
                .and(createdAfter(resolveFromDate(period)));
    }

    private Specification<AdminAuditLog> matchesSearch(String search) {
        String query = blankToNull(search);
        if (query == null) {
            return null;
        }
        String like = "%" + query.toLowerCase(Locale.ROOT) + "%";
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("action"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("detail"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("resourceType"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("resourceId"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("ipAddress"), "")), like));
    }

    private Specification<AdminAuditLog> equalsField(String field, String value) {
        if (value == null) {
            return null;
        }
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
    }

    private Specification<AdminAuditLog> createdAfter(LocalDateTime fromDate) {
        if (fromDate == null) {
            return null;
        }
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    @Transactional
    public void record(UUID actorId,
                       String actionType,
                       String action,
                       String resourceType,
                       String resourceId,
                       AdminActionResultResponse result,
                       String detail,
                       HttpServletRequest request) {
        record(actorId, actionType, action, resourceType, resourceId, result != null && result.success(), detail, request);
    }

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
            AdminAuditLog logEntry = new AdminAuditLog();
            logEntry.setActorId(actorId);
            logEntry.setActionType(normalizeActionType(actionType));
            logEntry.setAction(safe(action, "Admin action"));
            logEntry.setResourceType(safe(resourceType, "ADMIN"));
            logEntry.setResourceId(blankToNull(resourceId));
            logEntry.setResult(success ? "success" : "error");
            logEntry.setDetail(safe(detail, ""));
            logEntry.setIpAddress(resolveIpAddress(request));
            logEntry.setUserAgent(request == null ? "" : safe(request.getHeader("User-Agent"), ""));
            repository.save(logEntry);
        } catch (Exception ex) {
            log.warn("Failed to write admin audit log", ex);
        }
    }

    private AdminAuditLogResponse toResponse(AdminAuditLog logEntry) {
        String result = safe(logEntry.getResult(), "success");
        return new AdminAuditLogResponse(
                logEntry.getId().toString(),
                logEntry.getActorId() == null ? "" : logEntry.getActorId().toString(),
                logEntry.getActionType(),
                logEntry.getAction(),
                logEntry.getResourceType(),
                safe(logEntry.getResourceId(), ""),
                result,
                safe(logEntry.getDetail(), ""),
                formatRelativeTime(logEntry.getCreatedAt()),
                buildMeta(logEntry),
                "error".equals(result) ? "danger" : "success",
                "error".equals(result) ? "Lỗi" : "Thành công",
                safe(logEntry.getIpAddress(), ""),
                safe(logEntry.getUserAgent(), ""),
                logEntry.getCreatedAt() == null ? "" : logEntry.getCreatedAt().format(DATE_TIME_FORMAT));
    }

    private String buildMeta(AdminAuditLog logEntry) {
        String actor = logEntry.getActorId() == null ? "unknown" : logEntry.getActorId().toString();
        String ip = safe(logEntry.getIpAddress(), "");
        return ip.isBlank() ? actor : actor + " · " + ip;
    }

    private String formatRelativeTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 1) {
            return "Vừa xong";
        }
        if (minutes < 60) {
            return minutes + " phút trước";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " giờ trước";
        }
        return createdAt.format(DATE_TIME_FORMAT);
    }

    private LocalDateTime resolveFromDate(String period) {
        String normalized = period == null ? "" : period.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "today" -> LocalDate.now().atStartOfDay();
            case "7d" -> LocalDateTime.now().minusDays(7);
            case "month" -> LocalDate.now().withDayOfMonth(1).atStartOfDay();
            default -> null;
        };
    }

    private String normalizeFilter(String value) {
        String normalized = blankToNull(value);
        if (normalized == null || "all".equalsIgnoreCase(normalized)) {
            return null;
        }
        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeActionType(String actionType) {
        String normalized = blankToNull(actionType);
        return normalized == null ? "update" : normalized.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safe(String value, String fallback) {
        return value == null ? fallback : value;
    }
}

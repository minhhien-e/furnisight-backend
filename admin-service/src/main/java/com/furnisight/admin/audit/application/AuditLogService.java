package com.furnisight.admin.audit.application;

import com.furnisight.admin.account.infrastructure.grpc.AdminUserGrpcClient;
import com.furnisight.admin.user.AccountDetailResponse;
import com.furnisight.admin.user.AccountDto;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.audit.web.dto.response.AuditLogPageResponse;
import com.furnisight.admin.audit.web.dto.response.AuditLogResponse;
import com.furnisight.admin.audit.infrastructure.persistence.AuditLog;
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
    public AuditLogPageResponse getLogs(String search, String type, String result, String period, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> logs = repository.findAll(buildSpec(search, type, result, period), pageable);
        Map<UUID, String> actorNames = resolveActorNames(logs.getContent());

        return new AuditLogPageResponse(
                logs.getContent().stream().map(logEntry -> toResponse(logEntry, actorNames)).toList(),
                logs.getTotalElements(),
                safePage,
                safeSize,
                logs.getTotalPages());
    }

    private Specification<AuditLog> buildSpec(String search, String type, String result, String period) {
        return Specification
                .where(matchesSearch(search))
                .and(equalsField("actionType", normalizeFilter(type)))
                .and(equalsField("result", normalizeFilter(result)))
                .and(createdAfter(resolveFromDate(period)));
    }

    private Specification<AuditLog> matchesSearch(String search) {
        String query = blankToNull(search);
        if (query == null) {
            return null;
        }
        String like = "%" + query.toLowerCase(Locale.ROOT) + "%";
        List<UUID> actorIds = resolveActorIdsBySearch(query);
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("action"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("detail"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("resourceType"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("resourceId"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("ipAddress"), "")), like),
                actorIds.isEmpty() ? criteriaBuilder.disjunction() : root.get("actorId").in(actorIds));
    }

    private Specification<AuditLog> equalsField(String field, String value) {
        if (value == null) {
            return null;
        }
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
    }

    private Specification<AuditLog> createdAfter(LocalDateTime fromDate) {
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
                       ActionResultResponse result,
                       String detail,
                       HttpServletRequest request) {
        record(actorId, actionType, action, resourceType, resourceId, result != null && result.success(), detail, request);
    }

    @Transactional
    public void recordOrderEvent(
            UUID actorId, String orderCode, String previousStatus,
            String nextStatus, String trackingCode, String note
    ) {
        String detail = "Trạng thái: " + safe(previousStatus, "") + " -> " + safe(nextStatus, "");
        if (trackingCode != null && !trackingCode.isBlank()) {
            detail += "; Mã vận đơn: " + trackingCode;
        }
        if (note != null && !note.isBlank()) {
            detail += "; Ghi chú: " + note;
        }
        record(actorId, "update", "Cập nhật trạng thái đơn hàng", "ORDER",
                orderCode, true, detail, null);
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
            AuditLog logEntry = new AuditLog();
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

    private AuditLogResponse toResponse(AuditLog logEntry, Map<UUID, String> actorNames) {
        String result = safe(logEntry.getResult(), "success");
        String actorName = resolveActorName(logEntry.getActorId(), actorNames);
        return new AuditLogResponse(
                logEntry.getId().toString(),
                logEntry.getActorId() == null ? "" : logEntry.getActorId().toString(),
                actorName,
                logEntry.getActionType(),
                logEntry.getAction(),
                logEntry.getResourceType(),
                safe(logEntry.getResourceId(), ""),
                result,
                safe(logEntry.getDetail(), ""),
                formatRelativeTime(logEntry.getCreatedAt()),
                buildMeta(logEntry, actorName),
                "error".equals(result) ? "danger" : "success",
                "error".equals(result) ? "Lỗi" : "Thành công",
                safe(logEntry.getIpAddress(), ""),
                safe(logEntry.getUserAgent(), ""),
                logEntry.getCreatedAt() == null ? "" : logEntry.getCreatedAt().format(DATE_TIME_FORMAT));
    }

    private String buildMeta(AuditLog logEntry, String actorName) {
        String actor = actorName.isBlank()
                ? (logEntry.getActorId() == null ? "unknown" : logEntry.getActorId().toString())
                : actorName;
        String ip = safe(logEntry.getIpAddress(), "");
        return ip.isBlank() ? actor : actor + " · " + ip;
    }

    private String resolveActorName(UUID actorId, Map<UUID, String> actorNames) {
        if (actorId == null) {
            return "";
        }
        return actorNames.computeIfAbsent(actorId, this::fetchActorName);
    }

    private String fetchActorName(UUID actorId) {
        try {
            AccountDetailResponse account = userClient.getAccountById(actorId);
            String displayName = safe(account.getName(), "").trim();
            if (!displayName.isBlank()) {
                return displayName;
            }
            String fullName = (safe(account.getLastName(), "") + " " + safe(account.getFirstName(), "")).trim();
            if (!fullName.isBlank()) {
                return fullName;
            }
            String username = safe(account.getUsername(), "").trim();
            if (!username.isBlank()) {
                return username;
            }
            return safe(account.getEmail(), "").trim();
        } catch (Exception ex) {
            log.debug("Failed to resolve audit actor name {}", actorId, ex);
            return "";
        }
    }

    private Map<UUID, String> resolveActorNames(List<AuditLog> logs) {
        Map<UUID, String> actorNames = new HashMap<>();
        LinkedHashSet<UUID> unresolvedIds = logs.stream()
                .map(AuditLog::getActorId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (unresolvedIds.isEmpty()) {
            return actorNames;
        }

        try {
            int page = 1;
            int totalPages;
            do {
                var response = userClient.getAccounts(page, 200, "", "");
                response.getAccountsList().forEach(account -> putActorName(actorNames, unresolvedIds, account));
                totalPages = Math.max(response.getTotalPages(), 1);
                page++;
            } while (!unresolvedIds.isEmpty() && page <= totalPages);
        } catch (Exception ex) {
            log.debug("Failed to resolve audit actor names from account list", ex);
        }

        unresolvedIds.forEach(actorId -> actorNames.put(actorId, fetchActorName(actorId)));
        return actorNames;
    }

    private void putActorName(Map<UUID, String> actorNames, LinkedHashSet<UUID> unresolvedIds, AccountDto account) {
        UUID accountId = parseUuid(account.getId());
        if (accountId == null || !unresolvedIds.contains(accountId)) {
            return;
        }

        String actorName = firstNotBlank(account.getName(), account.getUsername(), account.getEmail());
        actorNames.put(accountId, actorName);
        unresolvedIds.remove(accountId);
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            String normalized = safe(value, "").trim();
            if (!normalized.isBlank()) {
                return normalized;
            }
        }
        return "";
    }

    private List<UUID> resolveActorIdsBySearch(String query) {
        try {
            return userClient.getAccounts(1, 50, query, "")
                    .getAccountsList()
                    .stream()
                    .map(account -> parseUuid(account.getId()))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        } catch (Exception ex) {
            log.debug("Failed to resolve audit actor ids for query {}", query, ex);
            return List.of();
        }
    }

    private UUID parseUuid(String value) {
        try {
            return value == null || value.isBlank() ? null : UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
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

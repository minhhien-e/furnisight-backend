package com.furnisight.admin.audit.infrastructure.persistence;

import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.Locale;

public class AuditLogSpecifications {

    public static Specification<AuditLog> buildSpec(String search, String type, String result, LocalDateTime fromDate) {
        return Specification
                .where(matchesSearch(search))
                .and(equalsField("actionType", type))
                .and(equalsField("result", result))
                .and(createdAfter(fromDate));
    }

    private static Specification<AuditLog> matchesSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String like = "%" + search.toLowerCase(Locale.ROOT) + "%";
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("action"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("detail"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("resourceType"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("resourceId"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("ipAddress"), "")), like),
                criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("actorName"), "")), like)
        );
    }

    private static Specification<AuditLog> equalsField(String field, String value) {
        if (value == null) {
            return null;
        }
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
    }

    private static Specification<AuditLog> createdAfter(LocalDateTime fromDate) {
        if (fromDate == null) {
            return null;
        }
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }
}

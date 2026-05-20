package com.furnisight.catalog.infrastructure.database.repository.impl.review;

import com.furnisight.catalog.application.review.dto.ReviewProjection;
import com.furnisight.catalog.application.review.port.out.repository.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository("jdbcAdapter")
@RequiredArgsConstructor
public class ReviewReadRepositoryImpl implements ReviewQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<ReviewProjection> findByProductId(UUID productId, Integer page, Integer size) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;
        int offset = pageNum * pageSize;

        String sql = "SELECT id, user_id, product_id, title, content_text, rating, status, created_at " +
                "FROM reviews " +
                "WHERE product_id = :productId AND status IN (:statuses) " +
                "ORDER BY created_at DESC " +
                "LIMIT :limit OFFSET :offset";

        Map<String, Object> params = Map.of(
                "productId", productId,
                "statuses", List.of("PENDING", "VISIBLE"),
                "limit", pageSize,
                "offset", offset
        );

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new ReviewProjection(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("user_id"),
                (UUID) rs.getObject("product_id"),
                rs.getString("title"),
                rs.getString("content_text"),
                rs.getInt("rating"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ));
    }
}

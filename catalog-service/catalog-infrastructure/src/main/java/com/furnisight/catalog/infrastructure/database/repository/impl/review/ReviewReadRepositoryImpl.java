package com.furnisight.catalog.infrastructure.database.repository.impl.review;

import com.furnisight.catalog.application.review.dto.response.ReviewResponse;
import com.furnisight.catalog.application.review.port.out.repository.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.furnisight.catalog.infrastructure.integration.remote.RemoteMediaUrlResolver;

@Repository("jdbcAdapter")
@RequiredArgsConstructor
public class ReviewReadRepositoryImpl implements ReviewQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RemoteMediaUrlResolver mediaUrlResolver;

    @Override
    public List<ReviewResponse> findByProductId(UUID productId, Integer page, Integer size) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;
        int offset = pageNum * pageSize;

        String sql = "SELECT id, user_id, user_name, user_avatar_media_id, product_id, title, content_text, rating, status, created_at " +
                "FROM reviews " +
                "WHERE product_id = :productId AND status::text IN (:statuses) " +
                "ORDER BY created_at DESC " +
                "LIMIT :limit OFFSET :offset";

        Map<String, Object> params = Map.of(
                "productId", productId,
                "statuses", List.of("PENDING", "VISIBLE"),
                "limit", pageSize,
                "offset", offset
        );

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            UUID mediaId = rs.getObject("user_avatar_media_id", UUID.class);
            String avatarUrl = null;
            if (mediaId != null) {
                avatarUrl = mediaUrlResolver.resolveUrl(mediaId).orElse(null);
            }
            return new ReviewResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getString("user_name"),
                avatarUrl,
                rs.getObject("product_id", UUID.class),
                rs.getString("title"),
                rs.getString("content_text"),
                rs.getInt("rating"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()
            );
        });
    }

    @Override
    public List<ReviewResponse> findTopRandomReviews(int limit) {
        String sql = "SELECT id, user_id, user_name, user_avatar_media_id, product_id, title, content_text, rating, status, created_at " +
                "FROM reviews " +
                "WHERE rating >= 4 AND status::text = 'VISIBLE' " +
                "ORDER BY rating DESC, RANDOM() " +
                "LIMIT :limit";

        Map<String, Object> params = Map.of("limit", limit);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            UUID mediaId = rs.getObject("user_avatar_media_id", UUID.class);
            String avatarUrl = null;
            if (mediaId != null) {
                avatarUrl = mediaUrlResolver.resolveUrl(mediaId).orElse(null);
            }
            return new ReviewResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getString("user_name"),
                avatarUrl,
                rs.getObject("product_id", UUID.class),
                rs.getString("title"),
                rs.getString("content_text"),
                rs.getInt("rating"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()
            );
        });
    }
}

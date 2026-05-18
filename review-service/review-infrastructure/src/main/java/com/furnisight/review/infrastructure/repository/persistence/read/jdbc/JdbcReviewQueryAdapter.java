package com.furnisight.review.infrastructure.repository.persistence.read.jdbc;

import com.furnisight.review.core.dto.ReviewResponse;
import com.furnisight.review.core.repository.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;@Repository("jdbcAdapter")
@RequiredArgsConstructor
public class JdbcReviewQueryAdapter implements ReviewQueryRepository {

    private final JdbcTemplate jdbc;

    @Override
    public List<ReviewResponse> findByProductId(UUID productId, Integer page, Integer size) {
        int limit = (size != null) ? size : 10;
        int offset = (page != null) ? page * limit : 0;

        String sql = "SELECT id, user_id, product_id, title, content_text, rating, status, created_at " +
            "FROM reviews WHERE product_id = ? AND status = 'PENDING' OR status = 'VISIBLE' " +
            "ORDER BY created_at DESC LIMIT ? OFFSET ?";

        return jdbc.query(sql, (rs, rowNum) -> new ReviewResponse(
            rs.getObject("id", UUID.class),
            rs.getObject("user_id", UUID.class),
            rs.getObject("product_id", UUID.class),
            rs.getString("title"),
            rs.getString("content_text"),
            rs.getInt("rating"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime()
        ), productId, limit, offset);
    }
}

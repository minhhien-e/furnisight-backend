package com.furnisight.review.adapter.database.repository.impl.review;

import com.furnisight.review.application.review.dto.response.ProductReviewStatResponse;
import com.furnisight.review.application.review.dto.response.ReviewResponse;
import com.furnisight.review.application.review.dto.response.ReviewSentimentStatsResponse;
import com.furnisight.review.application.review.port.out.repository.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.furnisight.review.adapter.integration.remote.RemoteMediaUrlResolver;

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

        String sql = "SELECT id, user_id, user_name, user_avatar_media_id, product_id, order_item_id, title, content_text, rating, status, created_at " +
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
                rs.getObject("order_item_id", UUID.class),
                rs.getString("title"),
                rs.getString("content_text"),
                rs.getInt("rating"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()
            );
        });
    }

    @Override
    public List<ReviewResponse> findByUserIdAndOrderItemIds(UUID userId, Collection<UUID> orderItemIds) {
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            return List.of();
        }

        String sql = "SELECT id, user_id, user_name, user_avatar_media_id, product_id, order_item_id, title, content_text, rating, status, created_at " +
                "FROM reviews " +
                "WHERE user_id = :userId AND order_item_id IN (:orderItemIds) " +
                "ORDER BY created_at DESC";

        return jdbcTemplate.query(sql, Map.of(
                "userId", userId,
                "orderItemIds", orderItemIds
        ), (rs, rowNum) -> {
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
                    rs.getObject("order_item_id", UUID.class),
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
        String sql = "SELECT id, user_id, user_name, user_avatar_media_id, product_id, order_item_id, title, content_text, rating, status, created_at " +
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
                rs.getObject("order_item_id", UUID.class),
                rs.getString("title"),
                rs.getString("content_text"),
                rs.getInt("rating"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()
            );
        });
    }

    @Override
    public List<ProductReviewStatResponse> findProductStats(Collection<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT product_id,
                       COALESCE(AVG(rating), 0) AS average_rating,
                       COUNT(*) AS rating_count,
                       COUNT(*) AS visible_review_count
                FROM reviews
                WHERE status::text = 'VISIBLE'
                  AND product_id IN (:productIds)
                GROUP BY product_id
                """;

        return jdbcTemplate.query(sql, Map.of("productIds", productIds), (rs, rowNum) ->
                new ProductReviewStatResponse(
                        rs.getObject("product_id", UUID.class),
                        rs.getDouble("average_rating"),
                        rs.getInt("rating_count"),
                        rs.getInt("visible_review_count")
                )
        );
    }

    @Override
    public ReviewSentimentStatsResponse findReviewSentimentStats() {
        String aggregateSql = """
                SELECT COUNT(*) AS total_reviews,
                       COUNT(*) FILTER (WHERE sentiment_status = 'COMPLETED') AS analyzed_reviews,
                       COUNT(*) FILTER (WHERE sentiment_status = 'PENDING') AS pending_reviews,
                       COUNT(*) FILTER (WHERE sentiment_status = 'FAILED') AS failed_reviews,
                       COUNT(*) FILTER (WHERE sentiment = 'POSITIVE') AS positive_count,
                       COUNT(*) FILTER (WHERE sentiment = 'NEUTRAL') AS neutral_count,
                       COUNT(*) FILTER (WHERE sentiment = 'NEGATIVE') AS negative_count
                FROM reviews
                """;

        Map<String, Object> aggregate = jdbcTemplate.queryForMap(aggregateSql, Map.of());

        String topNegativeSql = """
                SELECT product_id,
                       CAST(product_id AS TEXT) AS product_name,
                       COUNT(*) FILTER (WHERE sentiment = 'NEGATIVE') AS negative_count,
                       COALESCE(
                           (COUNT(*) FILTER (WHERE sentiment = 'NEGATIVE'))::double precision / NULLIF(COUNT(*) FILTER (WHERE status::text = 'VISIBLE'), 0),
                           0
                       ) AS negative_ratio,
                       COUNT(*) FILTER (WHERE status::text = 'VISIBLE') AS visible_review_count,
                       COALESCE(AVG(rating) FILTER (WHERE status::text = 'VISIBLE'), 0) AS average_rating
                FROM reviews
                GROUP BY product_id
                HAVING COUNT(*) FILTER (WHERE sentiment = 'NEGATIVE') > 0
                ORDER BY negative_count DESC, negative_ratio DESC
                LIMIT 5
                """;

        List<ReviewSentimentStatsResponse.TopNegativeProductResponse> topNegativeProducts = jdbcTemplate.query(
                topNegativeSql,
                Map.of(),
                (rs, rowNum) -> new ReviewSentimentStatsResponse.TopNegativeProductResponse(
                        rs.getObject("product_id", UUID.class),
                        rs.getString("product_name"),
                        rs.getLong("negative_count"),
                        rs.getDouble("negative_ratio"),
                        rs.getLong("visible_review_count"),
                        rs.getDouble("average_rating")
                )
        );

        return new ReviewSentimentStatsResponse(
                toLong(aggregate.get("total_reviews")),
                toLong(aggregate.get("analyzed_reviews")),
                toLong(aggregate.get("pending_reviews")),
                toLong(aggregate.get("failed_reviews")),
                toLong(aggregate.get("positive_count")),
                toLong(aggregate.get("neutral_count")),
                toLong(aggregate.get("negative_count")),
                topNegativeProducts
        );
    }

    private long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}


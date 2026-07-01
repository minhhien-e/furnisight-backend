package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.out.FavoriteProductReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FavoriteProductReadRepositoryImpl implements FavoriteProductReadRepository {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<ProductResponse> findTopFavoritedProductsSince(LocalDateTime since, int limit) {
        if (since == null || limit <= 0) {
            return List.of();
        }

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    p.image AS product_image,
                    p.attributes AS product_attributes,
                    p.metadata AS product_metadata,
                    c.name AS category_name,
                    MIN(pv.price) AS product_price,
                    COUNT(f.id) AS fav_count
                FROM product_favorite_logs f
                JOIN products p ON f.product_id = p.id
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN product_variants pv ON pv.product_id = p.id
                WHERE f.created_at >= :since
                GROUP BY
                    p.id,
                    p.name,
                    p.slug,
                    p.image,
                    p.attributes,
                    p.metadata,
                    c.name
                ORDER BY fav_count DESC
                LIMIT :limit
                """;

        return jdbcTemplate.query(
                sql,
                Map.of(
                        "since", since,
                        "limit", limit),
                (rs, rowNum) -> {
                    UUID id = (UUID) rs.getObject("product_id");

                    String name = normalizeText(rs.getString("product_name"), "Sản phẩm");
                    String slug = normalizeText(rs.getString("product_slug"), id.toString());
                    String categoryName = normalizeText(rs.getString("category_name"), "Sản phẩm");

                    Double price = getNullableDouble(rs, "product_price");
                    if (price == null) {
                        price = 0.0;
                    }

                    String image = normalizeText(rs.getString("product_image"), null);

                    Map<String, Object> attributes = parseJsonMapObject(rs.getString("product_attributes"));

                    if (image == null && attributes.get("image") != null) {
                        image = attributes.get("image").toString();
                    }

                    Map<String, Object> metadata = parseJsonMapObject(rs.getString("product_metadata"));

                    return ProductResponse.builder()
                            .id(id)
                            .slug(slug)
                            .name(name)
                            .categoryName(categoryName)
                            .price(price)
                            .image(image)
                            .rating(4.8)
                            .ratingCount(120)
                            .build();
                });
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim();
    }

    private Double getNullableDouble(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    private Map<String, Object> parseJsonMapObject(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            log.warn("Failed to parse JSON map object: {}", json, e);
            return new HashMap<>();
        }
    }

}

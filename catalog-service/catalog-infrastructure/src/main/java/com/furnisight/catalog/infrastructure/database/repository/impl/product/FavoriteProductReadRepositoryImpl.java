package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.projection.ProductSummaryProjection;
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

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<ProductSummaryProjection> findTopFavoritedProductsSince(LocalDateTime since, int limit) {
        String sql = "SELECT p.id as product_id, p.name as product_name, p.slug as product_slug, " +
                     "p.attributes as product_attributes, p.metadata as product_metadata, " +
                     "c.name as category_name, " +
                     "(SELECT MIN(pv.price) FROM product_variants pv WHERE pv.product_id = p.id) as product_price, " +
                     "COUNT(f.id) as fav_count " +
                     "FROM product_favorite_logs f " +
                     "JOIN products p ON f.product_id = p.id " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE f.created_at >= :since " +
                     "GROUP BY p.id, c.name " +
                     "ORDER BY fav_count DESC " +
                     "LIMIT :limit";

        return jdbcTemplate.query(sql, Map.of("since", since, "limit", limit), (rs, rowNum) -> {
            UUID id = (UUID) rs.getObject("product_id");
            String productName = rs.getString("product_name");
            String productSlug = rs.getString("product_slug");
            String categoryName = rs.getString("category_name");
            if (categoryName == null) {
                categoryName = "Sản phẩm";
            }
            Double price = rs.getDouble("product_price");
            if (rs.wasNull()) {
                price = 0.0;
            }

            Map<String, Object> attributes = parseJsonMapObject(rs.getString("product_attributes"));
            String imageUrl = null;
            if (attributes != null && attributes.get("image") != null) {
                imageUrl = attributes.get("image").toString();
            }

            Map<String, Object> metadata = parseJsonMapObject(rs.getString("product_metadata"));
            List<String> tagsList = List.of("new");
            if (metadata != null && metadata.get("tags") instanceof List<?> list) {
                tagsList = list.stream().map(Object::toString).toList();
            }

            return ProductSummaryProjection.builder()
                    .id(id)
                    .slug(productSlug != null ? productSlug : id.toString())
                    .name(productName)
                    .categoryName(categoryName)
                    .price(price)
                    .oldPrice(price > 0 ? price * 1.2 : null)
                    .image(imageUrl)
                    .rating(4.8)
                    .ratingCount(120)
                    .tags(tagsList)
                    .build();
        });
    }

    private Map<String, Object> parseJsonMapObject(String json) {
        if (json == null || json.isBlank())
            return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON map object", e);
            return new HashMap<>();
        }
    }
}

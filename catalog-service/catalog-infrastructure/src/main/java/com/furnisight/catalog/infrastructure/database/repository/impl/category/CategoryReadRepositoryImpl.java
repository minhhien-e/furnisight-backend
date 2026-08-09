package com.furnisight.catalog.infrastructure.database.repository.impl.category;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.category.port.out.CategoryReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryReadRepositoryImpl implements CategoryReadRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<CategoryResponse> findCategoryDetailBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }

        String normalizedSlug = slug.trim().toLowerCase();

        String sql = """
                SELECT *
                FROM categories
                WHERE LOWER(slug) = :slug
                LIMIT 1
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("slug", normalizedSlug),
                rs -> {
                    if (rs.next()) {
                        return Optional.of(mapRowToDto(rs));
                    }
                    return Optional.empty();
                });
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "categories", key = "'all'")
    public List<CategoryResponse> findAllCategories() {
        String sql = """
                SELECT c.*, COUNT(p.id) as real_product_count
                FROM categories c
                LEFT JOIN products p ON c.id = p.category_id
                GROUP BY c.id
                ORDER BY c.name ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CategoryResponse response = mapRowToDto(rs);
            response.setProductCount(rs.getInt("real_product_count"));
            return response;
        });
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "categories", key = "'root'")
    public List<CategoryResponse> findRootCategories() {
        String sql = """
                SELECT *
                FROM categories
                WHERE parent_id IS NULL
                ORDER BY name ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToDto(rs));
    }

    @Override
    public List<CategoryResponse> findSubcategoriesByParentSlug(String parentSlug) {
        if (parentSlug == null || parentSlug.isBlank()) {
            return List.of();
        }

        String sql = """
                SELECT child.*
                FROM categories child
                INNER JOIN categories parent ON child.parent_id = parent.id
                WHERE LOWER(parent.slug) = :parentSlug
                ORDER BY child.name ASC
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("parentSlug", parentSlug.trim().toLowerCase()),
                (rs, rowNum) -> mapRowToDto(rs));
    }

    @Override
    public Optional<CategoryResponse> findCategoryDetailById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT *
                FROM categories
                WHERE id = :id
                LIMIT 1
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("id", id),
                rs -> {
                    if (rs.next()) {
                        return Optional.of(mapRowToDto(rs));
                    }
                    return Optional.empty();
                });
    }

    @Override
    public long countCategories() {
        Long total = jdbcTemplate.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM categories", Long.class);
        return total == null ? 0L : total;
    }

    private CategoryResponse mapRowToDto(ResultSet rs) throws SQLException {
        java.sql.Timestamp createdTimestamp = rs.getTimestamp("created_at");
        return CategoryResponse.builder()
                .id((UUID) rs.getObject("id"))
                .name(rs.getString("name"))
                .slug(rs.getString("slug"))
                .path(rs.getString("path"))
                .parentId((UUID) rs.getObject("parent_id"))
                .roomTypeId((UUID) rs.getObject("room_type_id"))
                .productCount(rs.getInt("product_count"))
                .visible(rs.getBoolean("visible"))
                .description(rs.getString("description"))
                .imageUrl(rs.getString("image_url"))
                .iconUrl(rs.getString("icon_url"))
                .createdAt(createdTimestamp != null ? createdTimestamp.toLocalDateTime() : null)
                .build();
    }
}

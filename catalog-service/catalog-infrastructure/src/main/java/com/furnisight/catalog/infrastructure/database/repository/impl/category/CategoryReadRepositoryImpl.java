package com.furnisight.catalog.infrastructure.database.repository.impl.category;

import com.furnisight.catalog.application.category.dto.projection.CategoryDetailProjection;
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
    public Optional<CategoryDetailProjection> findCategoryDetailBySlug(String slug) {
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
    public List<CategoryDetailProjection> findAllCategories() {
        String sql = """
                SELECT *
                FROM categories
                ORDER BY name ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToDto(rs));
    }

    @Override
    public List<CategoryDetailProjection> findRootCategories() {
        String sql = """
                SELECT *
                FROM categories
                WHERE parent_id IS NULL
                ORDER BY name ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToDto(rs));
    }

    @Override
    public List<CategoryDetailProjection> findSubcategoriesByParentSlug(String parentSlug) {
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
    public Optional<CategoryDetailProjection> findCategoryDetailById(UUID id) {
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

    private CategoryDetailProjection mapRowToDto(ResultSet rs) throws SQLException {
        return CategoryDetailProjection.builder()
                .id((UUID) rs.getObject("id"))
                .name(rs.getString("name"))
                .slug(rs.getString("slug"))
                .path(rs.getString("path"))
                .parentId((UUID) rs.getObject("parent_id"))
                .productCount(rs.getInt("product_count"))
                .imageUrl(rs.getString("image_url"))
                .iconUrl(rs.getString("icon_url"))
                .build();
    }
}

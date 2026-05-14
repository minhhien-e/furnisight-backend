package com.furnisight.catalog.infrastructure.database.repository.impl.category;

import com.furnisight.catalog.application.category.dto.projection.CategoryDetailProjection;
import com.furnisight.catalog.application.category.port.out.CategoryReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryReadRepositoryImpl implements CategoryReadRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<CategoryDetailProjection> findCategoryDetailById(UUID categoryId) {
        String sql = "SELECT * FROM categories WHERE id = :id";
        return jdbcTemplate.query(sql, Map.of("id", categoryId), rs -> {
            if (rs.next()) {
                return Optional.of(mapRowToDto(rs));
            }
            return Optional.empty();
        });
    }

    @Override
    public List<CategoryDetailProjection> findAllCategories() {
        String sql = "SELECT * FROM categories";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToDto(rs));
    }

    private CategoryDetailProjection mapRowToDto(java.sql.ResultSet rs) throws java.sql.SQLException {
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

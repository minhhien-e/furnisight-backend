package com.furnisight.catalog.infrastructure.database.repository.impl.collection;

import com.furnisight.catalog.application.collection.dto.projection.CollectionDetailProjection;
import com.furnisight.catalog.application.collection.port.out.CollectionReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CollectionReadRepositoryImpl implements CollectionReadRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<CollectionDetailProjection> findAllCollections() {
        String sql = "SELECT * FROM collections";
        return jdbcTemplate.query(sql, (rs, rowNum) -> CollectionDetailProjection.builder()
                .id((UUID) rs.getObject("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .slug(rs.getString("slug"))
                .build());
    }
}

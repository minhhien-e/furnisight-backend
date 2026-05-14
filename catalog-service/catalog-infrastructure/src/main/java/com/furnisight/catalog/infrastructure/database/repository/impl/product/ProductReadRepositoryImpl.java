package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class ProductReadRepositoryImpl implements ProductReadRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ProductDetailProjection> findProductDetailById(UUID productId) {
        String sql = """
            SELECT p.*, c.name as category_name
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.id = :productId
            """;
        
        Map<String, Object> params = Map.of("productId", productId);
        
        return jdbcTemplate.query(sql, params, rs -> {
            if (rs.next()) {
                ProductDetailProjection dto = mapRowToProductDto(rs);
                List<ProductDetailProjection.VariantDto> variants = fetchVariants(productId);
                dto.setVariants(variants);
                if (!variants.isEmpty()) {
                    dto.setPrice(variants.get(0).getPrice());
                }
                return Optional.of(dto);
            }
            return Optional.empty();
        });
    }

    @Override
    public SearchProductsProjection searchProducts(String query, UUID categoryId, String status, int page, int size) {
        StringBuilder sqlBase = new StringBuilder("""
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE 1=1
            """);
        
        Map<String, Object> params = new HashMap<>();
        
        if (query != null && !query.isBlank()) {
            sqlBase.append(" AND (p.name ILIKE :query OR p.description ILIKE :query)");
            params.put("query", "%" + query + "%");
        }
        
        if (categoryId != null) {
            sqlBase.append(" AND p.category_id = :categoryId");
            params.put("categoryId", categoryId);
        }
        
        if (status != null) {
            sqlBase.append(" AND p.product_status = :status");
            params.put("status", status.toUpperCase());
        }

        // Count total
        String countSql = "SELECT COUNT(*) " + sqlBase.toString();
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        
        // Fetch data
        String dataSql = "SELECT p.*, c.name as category_name " + sqlBase.toString() + " LIMIT :limit OFFSET :offset";
        params.put("limit", size);
        params.put("offset", page * size);
        
        List<ProductDetailProjection> products = jdbcTemplate.query(dataSql, params, (rs, rowNum) -> mapRowToProductDto(rs));
        
        // Fetch variants for each product
        for (ProductDetailProjection product : products) {
            List<ProductDetailProjection.VariantDto> variants = fetchVariants(product.getId());
            product.setVariants(variants);
            if (!variants.isEmpty()) {
                product.setPrice(variants.get(0).getPrice());
            }
        }
        
        return SearchProductsProjection.builder()
                .products(products)
                .total(total != null ? total : 0)
                .build();
    }

    private ProductDetailProjection mapRowToProductDto(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, String> attributesMap = new HashMap<>();
        String attributesJson = rs.getString("attributes");
        if (attributesJson != null) {
            try {
                attributesMap = objectMapper.readValue(attributesJson, new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                // Log error or ignore
            }
        }
        
        return ProductDetailProjection.builder()
                .id((UUID) rs.getObject("id"))
                .shopId((UUID) rs.getObject("shop_id"))
                .categoryId((UUID) rs.getObject("category_id"))
                .categoryName(rs.getString("category_name"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .status(rs.getString("product_status"))
                .weight(rs.getDouble("weight"))
                .length(rs.getDouble("length"))
                .height(rs.getDouble("height"))
                .width(rs.getDouble("width"))
                .attributes(attributesMap)
                .image(attributesMap.get("image")) // Extract image from attributes
                .build();
    }

    @Override
    public List<ProductDetailProjection> findTopProducts(int limit) {
        String sql = """
            SELECT p.*, c.name as category_name
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.product_status = 'ACTIVE'
            ORDER BY p.view_count DESC
            LIMIT :limit
            """;
        
        List<ProductDetailProjection> products = jdbcTemplate.query(sql, Map.of("limit", limit), (rs, rowNum) -> mapRowToProductDto(rs));
        
        for (ProductDetailProjection product : products) {
            List<ProductDetailProjection.VariantDto> variants = fetchVariants(product.getId());
            product.setVariants(variants);
            if (!variants.isEmpty()) {
                product.setPrice(variants.get(0).getPrice());
            }
        }
        
        return products;
    }

    private List<ProductDetailProjection.VariantDto> fetchVariants(UUID productId) {
        String sql = "SELECT * FROM product_variants WHERE product_id = :productId";
        return jdbcTemplate.query(sql, Map.of("productId", productId), (rs, rowNum) -> 
            ProductDetailProjection.VariantDto.builder()
                .sku(rs.getString("sku"))
                .price(rs.getDouble("price"))
                .stockQuantity(rs.getInt("stock_quantity"))
                .build()
        );
    }
}


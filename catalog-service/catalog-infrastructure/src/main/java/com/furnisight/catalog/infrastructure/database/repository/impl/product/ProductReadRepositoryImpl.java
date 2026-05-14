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
    public SearchProductsProjection searchProducts(String query, String category, String status, int page, int size) {
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
        
        if (category != null && !category.isBlank()) {
            // Ho tro tim kiem theo slug hoac name cua category cha (hierarchical)
            sqlBase.append("""
                 AND p.category_id IN (
                    SELECT sub.id FROM categories sub 
                    WHERE sub.path LIKE (SELECT concat(path, '%') FROM categories WHERE slug = :category OR name = :category LIMIT 1)
                 )
                """);
            params.put("category", category);
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

        // --- CALCULATE FACETS ---
        
        // 1. Subcategories Facets
        String catFacetSql = "SELECT c.id, c.name, COUNT(p.id) as product_count " + 
                             sqlBase.toString() + 
                             " GROUP BY c.id, c.name";
        List<SearchProductsProjection.CategoryFacet> categoryFacets = jdbcTemplate.query(catFacetSql, params, (rs, rowNum) -> 
            SearchProductsProjection.CategoryFacet.builder()
                .id(rs.getString("id"))
                .label(rs.getString("name"))
                .count(rs.getLong("product_count"))
                .build()
        );

        // 2. Material Facets (aggregated from attributes JSONB)
        String materialFacetSql = "SELECT DISTINCT p.attributes->>'material' as material " + 
                                  sqlBase.toString() + 
                                  " AND p.attributes->>'material' IS NOT NULL";
        List<SearchProductsProjection.MaterialFacet> materialFacets = jdbcTemplate.query(materialFacetSql, params, (rs, rowNum) -> 
            SearchProductsProjection.MaterialFacet.builder()
                .id(rs.getString("material").toLowerCase().replace(" ", "-"))
                .label(rs.getString("material"))
                .build()
        );

        // 3. Color Facets (aggregated from attributes JSONB)
        String colorFacetSql = "SELECT DISTINCT p.attributes->>'color' as color " + 
                               sqlBase.toString() + 
                               " AND p.attributes->>'color' IS NOT NULL";
        List<SearchProductsProjection.ColorFacet> colorFacets = jdbcTemplate.query(colorFacetSql, params, (rs, rowNum) -> 
            SearchProductsProjection.ColorFacet.builder()
                .id(rs.getString("color").toLowerCase().replace(" ", "-"))
                .label(rs.getString("color"))
                .hex(getColorHex(rs.getString("color")))
                .build()
        );

        return SearchProductsProjection.builder()
                .products(products)
                .total(total != null ? total : 0)
                .facets(SearchProductsProjection.Facets.builder()
                        .categories(categoryFacets)
                        .materials(materialFacets)
                        .colors(colorFacets)
                        .build())
                .build();
    }

    private String getColorHex(String color) {
        if (color == null) return "#000000";
        return switch (color.toLowerCase()) {
            case "brown" -> "#8B4513";
            case "natural" -> "#F5DEB3";
            case "white" -> "#FFFFFF";
            case "black" -> "#000000";
            case "grey", "gray" -> "#808080";
            default -> "#CCCCCC";
        };
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


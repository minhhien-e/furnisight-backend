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
import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;

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
    public SearchProductsProjection searchProducts(SearchProductsQuery queryParam) {
        StringBuilder sqlBase = new StringBuilder("""
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE 1=1
            """);
        
        Map<String, Object> params = new HashMap<>();
        
        if (queryParam.getQ() != null && !queryParam.getQ().isBlank()) {
            sqlBase.append(" AND (p.name ILIKE :q OR p.description ILIKE :q)");
            params.put("q", "%" + queryParam.getQ() + "%");
        }

        String sqlBaseWithoutCategory = sqlBase.toString();
        
        if (queryParam.getCategory() != null && !queryParam.getCategory().isBlank()) {
            // Hierarchical search: include all products in this category and its subcategories
            sqlBase.append("""
                 AND p.category_id IN (
                    SELECT sub.id FROM categories sub 
                    WHERE sub.path LIKE (SELECT concat(path, '%') FROM categories WHERE slug = :category OR name = :category LIMIT 1)
                 )
                """);
            params.put("category", queryParam.getCategory());
        }
        
        if (queryParam.getStatus() != null && !queryParam.getStatus().isBlank()) {
            sqlBase.append(" AND p.product_status = :status");
            params.put("status", queryParam.getStatus().toUpperCase());
        }
        
        if (queryParam.getMaterials() != null && !queryParam.getMaterials().isEmpty()) {
            // Filter by variant material (variant.attributes->>'material')
            sqlBase.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND (");
            for (int i = 0; i < queryParam.getMaterials().size(); i++) {
                if (i > 0) sqlBase.append(" OR ");
                sqlBase.append("pv.attributes->>'material' = :mat" + i);
                params.put("mat" + i, queryParam.getMaterials().get(i));
            }
            sqlBase.append("))");
        }
        
        if (queryParam.getColors() != null && !queryParam.getColors().isEmpty()) {
            // Filter by variant color (variant.attributes->>'color')
            sqlBase.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND (");
            for (int i = 0; i < queryParam.getColors().size(); i++) {
                if (i > 0) sqlBase.append(" OR ");
                sqlBase.append("pv.attributes->>'color' = :col" + i);
                params.put("col" + i, queryParam.getColors().get(i));
            }
            sqlBase.append("))");
        }

        // --- NEW FILTERS ---

        // 4. Price Bands (based on variant price)
        if (queryParam.getPriceBands() != null && !queryParam.getPriceBands().isEmpty()) {
            sqlBase.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND (");
            for (int i = 0; i < queryParam.getPriceBands().size(); i++) {
                if (i > 0) sqlBase.append(" OR ");
                String band = queryParam.getPriceBands().get(i);
                switch (band) {
                    case "lt5m" -> sqlBase.append("pv.price < 5000000");
                    case "5-15m" -> sqlBase.append("(pv.price >= 5000000 AND pv.price <= 15000000)");
                    case "15-30m" -> sqlBase.append("(pv.price >= 15000000 AND pv.price <= 30000000)");
                    case "gt30m" -> sqlBase.append("pv.price > 30000000");
                }
            }
            sqlBase.append("))");
        }

        // 5. Price Slider (based on variant price)
        if (queryParam.getPriceSliderPct() != null && !queryParam.getPriceSliderPct().isEmpty()) {
            Double pct = queryParam.getPriceSliderPct().get(0);
            if (pct < 100) {
                double maxPrice = (pct / 100.0) * 50000000.0; // Assume max 50M
                sqlBase.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.price <= :maxPriceSlider)");
                params.put("maxPriceSlider", maxPrice);
            }
        }

        // 6. Min Star (Mocking since no rating column yet, let's assume all meet 3+ stars for now or use view_count)
        if (queryParam.getMinStar() != null && queryParam.getMinStar() > 0) {
            // For now, let's just use view_count as a proxy for rating if no rating col exists
            // Or we could say products with view_count > 50 are "4 star" and > 100 are "5 star"
            if (queryParam.getMinStar() >= 5) {
                sqlBase.append(" AND p.view_count >= 100");
            } else if (queryParam.getMinStar() >= 4) {
                sqlBase.append(" AND p.view_count >= 50");
            }
        }

        // 7. Sale Only (Mocking based on some logic, e.g., if price is < 10M)
        if (Boolean.TRUE.equals(queryParam.getSaleOnly())) {
             sqlBase.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.price < 10000000)");
        }

        // Count total
        String countSql = "SELECT COUNT(*) " + sqlBase.toString();
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        
        // Fetch data
        String orderBy = "ORDER BY p.created_at DESC";
        if (queryParam.getSort() != null) {
            switch (queryParam.getSort()) {
                case "popular" -> orderBy = "ORDER BY p.view_count DESC";
                case "newest" -> orderBy = "ORDER BY p.created_at DESC";
                case "rating" -> orderBy = "ORDER BY p.view_count DESC"; // Mocking rating sort with view_count for now
                // Price sorting requires joining variants, we will skip it for this quick fix and fallback to newest
            }
        }
        
        String dataSql = "SELECT p.*, c.name as category_name, c.slug as category_slug " + sqlBase.toString() + " " + orderBy + " LIMIT :limit OFFSET :offset";
        params.put("limit", queryParam.getSize() > 0 ? queryParam.getSize() : 24);
        params.put("offset", queryParam.getPage() * (queryParam.getSize() > 0 ? queryParam.getSize() : 24));
        
        List<ProductDetailProjection> products = jdbcTemplate.query(dataSql, params, (rs, rowNum) -> mapRowToProductDto(rs));
        
        // Fetch variants for each product and aggregate materials/colors/sizes/stock from them
        for (ProductDetailProjection product : products) {
            List<ProductDetailProjection.VariantDto> variants = fetchVariants(product.getId());
            product.setVariants(variants);
            if (!variants.isEmpty()) {
                product.setPrice(variants.get(0).getPrice());
                // Aggregate unique materials, colors, sizes from variant attributes
                product.setMaterials(variants.stream()
                    .map(ProductDetailProjection.VariantDto::getMaterial)
                    .filter(m -> m != null && !m.isBlank())
                    .distinct().collect(java.util.stream.Collectors.toList()));
                product.setColors(variants.stream()
                    .map(ProductDetailProjection.VariantDto::getColor)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct().collect(java.util.stream.Collectors.toList()));
                product.setSizes(variants.stream()
                    .map(ProductDetailProjection.VariantDto::getSize)
                    .filter(s -> s != null && !s.isBlank())
                    .distinct().collect(java.util.stream.Collectors.toList()));
                // Total stock across all variants
                product.setStock(variants.stream()
                    .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                    .sum());
            }
        }

        // --- CALCULATE FACETS ---
        
        // 1. Subcategories Facets - compute independent of current category selection so sidebar stays full
        // We use the count of products matching all other filters
        String catFacetSql = "SELECT c.id, c.slug, c.name, COUNT(p.id) as product_count " + 
                             sqlBaseWithoutCategory + 
                             " GROUP BY c.id, c.slug, c.name";
        List<SearchProductsProjection.CategoryFacet> categoryFacets = jdbcTemplate.query(catFacetSql, params, (rs, rowNum) -> 
            SearchProductsProjection.CategoryFacet.builder()
                .id(rs.getString("id"))
                .slug(rs.getString("slug"))
                .label(rs.getString("name"))
                .count(rs.getLong("product_count"))
                .build()
        );

        // 2. Material Facets - aggregated from variant attributes JSONB
        String materialFacetSql = """
            SELECT DISTINCT pv.attributes->>'material' as material, pv.attributes->>'materialLabel' as material_label
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.id
            """
            + sqlBase.toString().replace("FROM products p", "")
                                .replace("LEFT JOIN categories c ON p.category_id = c.id", "")
                                .replace("WHERE 1=1", "WHERE 1=1")
            + " AND pv.attributes->>'material' IS NOT NULL";

        // Simpler approach: just do a full scan for facets (filter-independent for better UX)
        String simpleMatFacetSql = """
            SELECT DISTINCT pv.attributes->>'material' as material,
                            pv.attributes->>'materialLabel' as material_label
            FROM product_variants pv
            WHERE pv.attributes->>'material' IS NOT NULL
            """;
        List<SearchProductsProjection.MaterialFacet> materialFacets = jdbcTemplate.query(simpleMatFacetSql, (rs, rowNum) -> 
            SearchProductsProjection.MaterialFacet.builder()
                .id(rs.getString("material"))
                .label(rs.getString("material_label") != null ? rs.getString("material_label") : rs.getString("material"))
                .build()
        );

        // 3. Color Facets - aggregated from variant attributes JSONB
        String simpleColorFacetSql = """
            SELECT DISTINCT pv.attributes->>'color' as color,
                            pv.attributes->>'colorLabel' as color_label
            FROM product_variants pv
            WHERE pv.attributes->>'color' IS NOT NULL
            """;
        List<SearchProductsProjection.ColorFacet> colorFacets = jdbcTemplate.query(simpleColorFacetSql, (rs, rowNum) -> 
            SearchProductsProjection.ColorFacet.builder()
                .id(rs.getString("color"))
                .label(rs.getString("color_label") != null ? rs.getString("color_label") : rs.getString("color"))
                .hex(getColorHex(rs.getString("color")))
                .build()
        );

        return SearchProductsProjection.builder()
                .products(products)
                .total(total != null ? total : 0)
                .page(queryParam.getPage() + 1) // 1-based indexing for UI, while offset uses 0-based
                .pageSize(queryParam.getSize() > 0 ? queryParam.getSize() : 24)
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
        
        String categoryIdStr = rs.getObject("category_id") != null ? rs.getObject("category_id").toString() : null;
        String categoryName = rs.getString("category_name");
        String categorySlug = null;
        try { categorySlug = rs.getString("category_slug"); } catch(Exception e) {} // In case it's not selected in some queries

        // materials and colors will be populated from variants after fetch
        String imageUrl = attributesMap.get("image");

        return ProductDetailProjection.builder()
                .id((UUID) rs.getObject("id"))
                .slug(rs.getObject("id").toString()) // Simple slug mock using id
                .shopId((UUID) rs.getObject("shop_id"))
                .categoryId((UUID) rs.getObject("category_id"))
                .categoryName(categoryName)
                .category(ProductDetailProjection.CategoryInfo.builder()
                        .id(categorySlug != null ? categorySlug : categoryIdStr)
                        .label(categoryName)
                        .build())
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .thumbnailUrl(imageUrl)
                .image(imageUrl)
                .gallery(imageUrl != null ? List.of(imageUrl, imageUrl) : new ArrayList<>())
                .rating(4.8) // Mock rating
                .ratingCount(120) // Mock rating count
                .stock(50) // Mock stock
                .tags(List.of("new", "sale")) // Mock tags
                .materials(new ArrayList<>()) // will be populated from variants
                .colors(new ArrayList<>())    // will be populated from variants
                .sizes(new ArrayList<>())     // will be populated from variants
                .stock(0)                     // will be aggregated from variants
                .supports3d(true)
                .collection("Premium 2026")
                .roomTypeHint(categoryName != null ? categoryName : "Room")
                .modelUrl("/models/sofa.glb") // Mock 3d model
                .breadcrumb(List.of(
                    ProductDetailProjection.Breadcrumb.builder().id("home").label("Trang chủ").build(),
                    ProductDetailProjection.Breadcrumb.builder().id(categorySlug != null ? categorySlug : categoryIdStr).label(categoryName != null ? categoryName : "Sản phẩm").build()
                ))
                .features(List.of("Thiết kế hiện đại", "Chất liệu cao cấp", "Bảo hành 12 tháng"))
                .specs(Map.of("Thương hiệu", "FurniSight", "Xuất xứ", "Việt Nam", "Bảo hành", "1 năm"))
                .reviews(new ArrayList<>())
                .qa(new ArrayList<>())
                .status(rs.getString("product_status"))
                .weight(rs.getDouble("weight"))
                .length(rs.getDouble("length"))
                .height(rs.getDouble("height"))
                .width(rs.getDouble("width"))
                .attributes(attributesMap)
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
        String sql = "SELECT * FROM product_variants WHERE product_id = :productId ORDER BY price ASC";
        return jdbcTemplate.query(sql, Map.of("productId", productId), (rs, rowNum) -> {
            // Parse variant attributes JSONB
            Map<String, String> variantAttrs = new HashMap<>();
            String variantAttrsJson = rs.getString("attributes");
            if (variantAttrsJson != null) {
                try {
                    variantAttrs = objectMapper.readValue(variantAttrsJson, new TypeReference<Map<String, String>>() {});
                } catch (Exception ignored) {}
            }
            return ProductDetailProjection.VariantDto.builder()
                .sku(rs.getString("sku"))
                .price(rs.getDouble("price"))
                .stockQuantity(rs.getInt("stock_quantity"))
                .color(variantAttrs.get("color"))
                .colorLabel(variantAttrs.get("colorLabel"))
                .material(variantAttrs.get("material"))
                .materialLabel(variantAttrs.get("materialLabel"))
                .size(variantAttrs.get("size"))
                .build();
        });
    }
}


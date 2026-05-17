package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductReadRepositoryImpl implements ProductReadRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ProductDetailProjection> findProductDetailById(UUID productId) {
        String sql = """
                SELECT p.*, c.name as category_name, c.slug as category_slug, col.name as collection_name,
                       parent_c.name as parent_category_name, parent_c.slug as parent_category_slug
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN categories parent_c ON c.parent_id = parent_c.id
                LEFT JOIN collections col ON p.collection_id = col.id
                WHERE p.id = :productId
                """;

        return jdbcTemplate.query(sql, Map.of("productId", productId), rs -> {
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
        Map<String, Object> params = new HashMap<>();
        StringBuilder sqlFilters = new StringBuilder(" WHERE 1=1");
        
        applySearchFilters(queryParam, sqlFilters, params);
        String sqlFiltersWithoutCategory = sqlFilters.toString();
        applyCategoryFilter(queryParam.getCategory(), sqlFilters, params);

        String fromClause = """
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN categories parent_c ON c.parent_id = parent_c.id
                LEFT JOIN collections col ON p.collection_id = col.id
                """;

        // 1. Count total products matching filters
        String countSql = "SELECT COUNT(p.id) " + fromClause + sqlFilters;
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);

        // 2. Fetch products for the current page
        String orderBy = getOrderByClause(queryParam.getSort());
        String dataSql = "SELECT p.*, c.name as category_name, c.slug as category_slug, col.name as collection_name, " 
                + "parent_c.name as parent_category_name, parent_c.slug as parent_category_slug "
                + fromClause + sqlFilters + " " + orderBy + " LIMIT :limit OFFSET :offset";
                
        int limit = queryParam.getSize() > 0 ? queryParam.getSize() : 24;
        params.put("limit", limit);
        params.put("offset", queryParam.getPage() * limit);

        List<ProductDetailProjection> products = jdbcTemplate.query(dataSql, params, (rs, rowNum) -> mapRowToProductDto(rs));

        // 3. Populate variants and aggregate fields (colors, materials, sizes, stock, etc.)
        populateProductVariantsAndAggregates(products);

        // 4. Calculate facets (Categories, Materials, Colors)
        SearchProductsProjection.Facets facets = calculateFacets(fromClause, sqlFiltersWithoutCategory, sqlFilters.toString(), params);

        return SearchProductsProjection.builder()
                .products(products)
                .total(total != null ? total : 0)
                .page(queryParam.getPage() + 1) // 1-based indexing for UI
                .pageSize(limit)
                .facets(facets)
                .build();
    }

    @Override
    public List<ProductDetailProjection> findTopProducts(int limit) {
        String sql = """
                SELECT p.*, c.name as category_name, c.slug as category_slug, col.name as collection_name,
                       parent_c.name as parent_category_name, parent_c.slug as parent_category_slug
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN categories parent_c ON c.parent_id = parent_c.id
                LEFT JOIN collections col ON p.collection_id = col.id
                WHERE p.product_status = 'ACTIVE'
                ORDER BY p.view_count DESC
                LIMIT :limit
                """;

        List<ProductDetailProjection> products = jdbcTemplate.query(sql, Map.of("limit", limit),
                (rs, rowNum) -> mapRowToProductDto(rs));

        populateProductVariantsAndAggregates(products);
        return products;
    }

    // ─── HELPER METHODS FOR SEARCH QUERY BUILDING ────────────────────────────

    private void applySearchFilters(SearchProductsQuery queryParam, StringBuilder sql, Map<String, Object> params) {
        if (queryParam.getQ() != null && !queryParam.getQ().isBlank()) {
            sql.append(" AND (p.name ILIKE :q OR p.description ILIKE :q)");
            params.put("q", "%" + queryParam.getQ().trim() + "%");
        }

        if (queryParam.getStatus() != null && !queryParam.getStatus().isBlank()) {
            sql.append(" AND p.product_status = :status");
            params.put("status", queryParam.getStatus().toUpperCase());
        }

        applyMaterialFilters(queryParam.getMaterials(), sql, params);
        applyColorFilters(queryParam.getColors(), sql, params);
        applyPriceBandFilters(queryParam.getPriceBands(), sql);
        applyPriceSliderFilter(queryParam.getPriceSliderPct(), sql, params);
        applyRatingFilter(queryParam.getMinStar(), sql);
        applySaleFilter(queryParam.getSaleOnly(), sql);
    }

    private void applyCategoryFilter(String category, StringBuilder sql, Map<String, Object> params) {
        if (category != null && !category.isBlank()) {
            sql.append("""
                     AND p.category_id IN (
                        SELECT sub.id FROM categories sub
                        WHERE sub.path LIKE (SELECT concat(path, '%') FROM categories WHERE slug = :category OR name = :category LIMIT 1)
                     )
                    """);
            params.put("category", category);
        }
    }

    private void applyMaterialFilters(List<String> materials, StringBuilder sql, Map<String, Object> params) {
        if (materials != null && !materials.isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND (");
            for (int i = 0; i < materials.size(); i++) {
                if (i > 0) sql.append(" OR ");
                String paramKey = "mat" + i;
                sql.append("pv.attributes->>'material' = :").append(paramKey);
                params.put(paramKey, materials.get(i));
            }
            sql.append("))");
        }
    }

    private void applyColorFilters(List<String> colors, StringBuilder sql, Map<String, Object> params) {
        if (colors != null && !colors.isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND (");
            for (int i = 0; i < colors.size(); i++) {
                if (i > 0) sql.append(" OR ");
                String paramKey = "col" + i;
                sql.append("pv.attributes->>'color' = :").append(paramKey);
                params.put(paramKey, colors.get(i));
            }
            sql.append("))");
        }
    }

    private void applyPriceBandFilters(List<String> priceBands, StringBuilder sql) {
        if (priceBands != null && !priceBands.isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND (");
            for (int i = 0; i < priceBands.size(); i++) {
                if (i > 0) sql.append(" OR ");
                String band = priceBands.get(i);
                switch (band) {
                    case "lt5m" -> sql.append("pv.price < 5000000");
                    case "5-15m" -> sql.append("(pv.price >= 5000000 AND pv.price <= 15000000)");
                    case "15-30m" -> sql.append("(pv.price >= 15000000 AND pv.price <= 30000000)");
                    case "gt30m" -> sql.append("pv.price > 30000000");
                }
            }
            sql.append("))");
        }
    }

    private void applyPriceSliderFilter(List<Double> priceSliderPct, StringBuilder sql, Map<String, Object> params) {
        if (priceSliderPct != null && !priceSliderPct.isEmpty()) {
            Double pct = priceSliderPct.get(0);
            if (pct < 100) {
                double maxPrice = (pct / 100.0) * 50000000.0; // Max 50M
                sql.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.price <= :maxPriceSlider)");
                params.put("maxPriceSlider", maxPrice);
            }
        }
    }

    private void applyRatingFilter(Integer minStar, StringBuilder sql) {
        if (minStar != null && minStar > 0) {
            // Mock rating filter based on view_count
            if (minStar >= 5) {
                sql.append(" AND p.view_count >= 100");
            } else if (minStar >= 4) {
                sql.append(" AND p.view_count >= 50");
            }
        }
    }

    private void applySaleFilter(Boolean saleOnly, StringBuilder sql) {
        if (Boolean.TRUE.equals(saleOnly)) {
            // Mock sale only filter (variants with price < 10M)
            sql.append(" AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.price < 10000000)");
        }
    }

    private String getOrderByClause(String sort) {
        if (sort == null) {
            return "ORDER BY p.created_at DESC";
        }
        return switch (sort) {
            case "popular" -> "ORDER BY p.view_count DESC";
            case "newest" -> "ORDER BY p.created_at DESC";
            case "rating" -> "ORDER BY p.view_count DESC"; // Mock rating sort with view_count
            default -> "ORDER BY p.created_at DESC";
        };
    }

    // ─── HELPER METHODS FOR VARIANTS POPULATION & FACETS ────────────────────

    private void populateProductVariantsAndAggregates(List<ProductDetailProjection> products) {
        for (ProductDetailProjection product : products) {
            List<ProductDetailProjection.VariantDto> variants = fetchVariants(product.getId());
            product.setVariants(variants);
            if (!variants.isEmpty()) {
                product.setPrice(variants.get(0).getPrice());
                
                // Aggregate distinct materials
                product.setMaterials(variants.stream()
                        .map(ProductDetailProjection.VariantDto::getMaterial)
                        .filter(m -> m != null && !m.isBlank())
                        .distinct()
                        .collect(Collectors.toList()));
                        
                // Aggregate distinct colors
                product.setColors(variants.stream()
                        .map(ProductDetailProjection.VariantDto::getColor)
                        .filter(c -> c != null && !c.isBlank())
                        .distinct()
                        .collect(Collectors.toList()));
                        
                // Aggregate distinct sizes
                product.setSizes(variants.stream()
                        .map(ProductDetailProjection.VariantDto::getSize)
                        .filter(s -> s != null && !s.isBlank())
                        .distinct()
                        .collect(Collectors.toList()));
                        
                // Aggregate total stock
                product.setStock(variants.stream()
                        .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                        .sum());
            }
        }
    }

    private SearchProductsProjection.Facets calculateFacets(String fromClause, String sqlFiltersWithoutCategory, String sqlFiltersWithCategory, Map<String, Object> params) {
        // 1. Categories Facets (Count matching products per category)
        String catFacetSql = "SELECT c.id, c.slug, c.name, COUNT(p.id) as product_count " 
                + fromClause + sqlFiltersWithoutCategory 
                + " GROUP BY c.id, c.slug, c.name";
                
        List<SearchProductsProjection.CategoryFacet> categoryFacets = jdbcTemplate.query(catFacetSql, params,
                (rs, rowNum) -> SearchProductsProjection.CategoryFacet.builder()
                        .id(rs.getString("id"))
                        .slug(rs.getString("slug"))
                        .label(rs.getString("name"))
                        .count(rs.getLong("product_count"))
                        .build());

        // 2. Materials Facets
        String simpleMatFacetSql = """
                SELECT DISTINCT pv.attributes->>'material' as material,
                                pv.attributes->>'materialLabel' as material_label
                FROM product_variants pv
                WHERE pv.attributes->>'material' IS NOT NULL
                """;
        List<SearchProductsProjection.MaterialFacet> materialFacets = jdbcTemplate.query(simpleMatFacetSql,
                (rs, rowNum) -> SearchProductsProjection.MaterialFacet.builder()
                        .id(rs.getString("material"))
                        .label(rs.getString("material_label") != null ? rs.getString("material_label") : rs.getString("material"))
                        .build());

        // 3. Colors Facets
        String simpleColorFacetSql = """
                SELECT DISTINCT pv.attributes->>'color' as color,
                                pv.attributes->>'colorLabel' as color_label
                FROM product_variants pv
                WHERE pv.attributes->>'color' IS NOT NULL
                """;
        List<SearchProductsProjection.ColorFacet> colorFacets = jdbcTemplate.query(simpleColorFacetSql,
                (rs, rowNum) -> SearchProductsProjection.ColorFacet.builder()
                        .id(rs.getString("color"))
                        .label(rs.getString("color_label") != null ? rs.getString("color_label") : rs.getString("color"))
                        .hex(getColorHex(rs.getString("color")))
                        .build());

        return SearchProductsProjection.Facets.builder()
                .categories(categoryFacets)
                .materials(materialFacets)
                .colors(colorFacets)
                .build();
    }

    private String getColorHex(String color) {
        if (color == null)
            return "#CCCCCC";
        return switch (color.toLowerCase()) {
            case "brown" -> "#8B4513";
            case "natural" -> "#F5DEB3";
            case "white" -> "#FFFFFF";
            case "black" -> "#000000";
            case "grey", "gray" -> "#808080";
            case "beige" -> "#F5F5DC";
            case "gold" -> "#D4AF37";
            case "green" -> "#2E7D32";
            case "blue" -> "#1565C0";
            default -> "#CCCCCC";
        };
    }

    private List<String> fetchGallery(UUID productId) {
        String sql = "SELECT image_url FROM product_images WHERE product_id = :productId ORDER BY sort_order ASC";
        try {
            return jdbcTemplate.query(sql, Map.of("productId", productId), (rs, rowNum) -> rs.getString("image_url"));
        } catch (Exception e) {
            log.error("Failed to fetch gallery for product {}", productId, e);
            return new ArrayList<>();
        }
    }

    private ProductDetailProjection mapRowToProductDto(ResultSet rs) throws SQLException {
        UUID productId = (UUID) rs.getObject("id");
        String categoryIdStr = rs.getObject("category_id") != null ? rs.getObject("category_id").toString() : null;
        String categoryName = rs.getString("category_name");
        String categorySlug = getNullableString(rs, "category_slug");
        String collectionName = getNullableString(rs, "collection_name");
        String parentCategoryName = getNullableString(rs, "parent_category_name");
        String parentCategorySlug = getNullableString(rs, "parent_category_slug");

        Map<String, String> attributesMap = parseJsonMap(rs.getString("attributes"));
        Map<String, Object> metadataMap = parseJsonMapGeneric(rs.getString("metadata"));
        Map<String, String> specsMap = parseJsonMap(rs.getString("specs"));

        String imageUrl = attributesMap.get("image");

        List<String> tagsList = metadataMap.get("tags") instanceof List<?> list 
                ? list.stream().map(Object::toString).toList() 
                : List.of("new", "sale");

        List<String> featuresList = metadataMap.get("features") instanceof List<?> list 
                ? list.stream().map(Object::toString).toList() 
                : List.of("Thiết kế hiện đại", "Chất liệu cao cấp", "Bảo hành 12 tháng");

        String modelUrlStr = metadataMap.get("model_url") != null 
                ? metadataMap.get("model_url").toString() 
                : "/models/sofa.glb";

        String collectionStr = collectionName != null ? collectionName : (metadataMap.get("collection") != null 
                ? metadataMap.get("collection").toString() 
                : null);

        List<String> galleryList = fetchGallery(productId);
        if (galleryList.isEmpty() && imageUrl != null) {
            galleryList = List.of(imageUrl, imageUrl);
        }

        List<ProductDetailProjection.Breadcrumb> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(ProductDetailProjection.Breadcrumb.builder().id("home").label("Trang chủ").build());
        if (parentCategoryName != null && !parentCategoryName.isEmpty()) {
            breadcrumbList.add(ProductDetailProjection.Breadcrumb.builder()
                    .id(parentCategorySlug != null ? parentCategorySlug : "all")
                    .label(parentCategoryName)
                    .build());
        }
        breadcrumbList.add(ProductDetailProjection.Breadcrumb.builder()
                .id(categorySlug != null ? categorySlug : categoryIdStr)
                .label(categoryName != null ? categoryName : "Sản phẩm")
                .build());

        return ProductDetailProjection.builder()
                .id(productId)
                .slug(productId.toString())
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
                .gallery(galleryList)
                .rating(4.8)
                .ratingCount(120)
                .stock(50)
                .tags(tagsList)
                .materials(new ArrayList<>())
                .colors(new ArrayList<>())
                .sizes(new ArrayList<>())
                .supports3d(true)
                .collection(collectionStr)
                .roomTypeHint(categoryName != null ? categoryName : "Room")
                .modelUrl(modelUrlStr)
                .breadcrumb(breadcrumbList)
                .features(featuresList)
                .specs(specsMap)
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

    private List<ProductDetailProjection.VariantDto> fetchVariants(UUID productId) {
        String sql = "SELECT * FROM product_variants WHERE product_id = :productId ORDER BY price ASC";
        return jdbcTemplate.query(sql, Map.of("productId", productId), (rs, rowNum) -> {
            Map<String, String> variantAttrs = parseJsonMap(rs.getString("attributes"));
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

    // ─── REUSABLE JSON PARSING UTILS ─────────────────────────────────────────

    private Map<String, String> parseJsonMap(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON map from value: {}", json, e);
            return new HashMap<>();
        }
    }

    private Map<String, Object> parseJsonMapGeneric(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse generic JSON map from value: {}", json, e);
            return new HashMap<>();
        }
    }

    private String getNullableString(ResultSet rs, String columnLabel) {
        try {
            return rs.getString(columnLabel);
        } catch (Exception e) {
            return null;
        }
    }
}

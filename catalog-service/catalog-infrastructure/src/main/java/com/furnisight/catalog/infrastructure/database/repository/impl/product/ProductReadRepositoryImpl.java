package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.projection.ProductSummaryProjection;
import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
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

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ProductDetailProjection> findProductDetailBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.category_id,
                    p.collection_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    p.description AS product_description,
                    p.product_status,
                    p.features AS product_features,
                    p.model_url,
                    p.supports_3d,
                    c.name AS category_name,
                    c.slug AS category_slug,
                    pc.name AS parent_category_name,
                    pc.slug AS parent_category_slug,
                    col.name AS collection_name,
                    COALESCE(rv.avg_rating, 0) AS avg_rating,
                    COALESCE(rv.review_count, 0) AS review_count
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN categories pc ON c.parent_id = pc.id
                LEFT JOIN collections col ON p.collection_id = col.id
                LEFT JOIN (
                    SELECT product_id, AVG(rating) AS avg_rating, COUNT(id) AS review_count
                    FROM reviews
                    WHERE status = 'VISIBLE'::review_status
                    GROUP BY product_id
                ) rv ON rv.product_id = p.id
                WHERE LOWER(p.slug) = :slug
                LIMIT 1
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("slug", slug.trim().toLowerCase()),
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    ProductDetailProjection dto = mapRowToProductDetail(rs);
                    hydrateProductDetail(dto);
                    return Optional.of(dto);
                });
    }

    @Override
    public Optional<ProductDetailProjection> findProductDetailById(UUID productId) {
        if (productId == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.category_id,
                    p.collection_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    p.description AS product_description,
                    p.product_status,
                    p.features AS product_features,
                    p.model_url,
                    p.supports_3d,
                    c.name AS category_name,
                    c.slug AS category_slug,
                    pc.name AS parent_category_name,
                    pc.slug AS parent_category_slug,
                    col.name AS collection_name,
                    COALESCE(rv.avg_rating, 0) AS avg_rating,
                    COALESCE(rv.review_count, 0) AS review_count
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN categories pc ON c.parent_id = pc.id
                LEFT JOIN collections col ON p.collection_id = col.id
                LEFT JOIN (
                    SELECT product_id, AVG(rating) AS avg_rating, COUNT(id) AS review_count
                    FROM reviews
                    WHERE status = 'VISIBLE'::review_status
                    GROUP BY product_id
                ) rv ON rv.product_id = p.id
                WHERE p.id = :productId
                LIMIT 1
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("productId", productId),
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    ProductDetailProjection dto = mapRowToProductDetail(rs);
                    hydrateProductDetail(dto);
                    return Optional.of(dto);
                });
    }

    @Override
    public SearchProductsProjection searchProducts(SearchProductsQuery queryParam) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        appendSearchFilter(whereClause, params, queryParam);
        appendStatusFilter(whereClause, params, queryParam);
        appendCategoryFilter(whereClause, params, queryParam);
        appendVariantFilters(whereClause, params, queryParam);
        appendPriceFilters(whereClause, params, queryParam);
        appendRatingFilters(whereClause, params, queryParam);

        Long total = countProducts(whereClause, params);

        int page = Math.max(queryParam.getPage(), 0);
        int size = queryParam.getSize() > 0 ? queryParam.getSize() : 24;
        int offset = page * size;

        params.put("limit", size);
        params.put("offset", offset);

        String mainSql = """
                SELECT
                    p.id AS product_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    c.name AS category_name,
                    MIN(pv.price) AS product_price,
                    (
                        SELECT pi.image_url
                        FROM product_images pi
                        WHERE pi.product_id = p.id
                        ORDER BY pi.position ASC
                        LIMIT 1
                    ) AS product_image,
                    COALESCE(rv.avg_rating, 0) AS product_rating,
                    COALESCE(rv.review_count, 0) AS product_rating_count
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN product_variants pv ON pv.product_id = p.id
                LEFT JOIN (
                    SELECT product_id, AVG(rating) AS avg_rating, COUNT(id) AS review_count
                    FROM reviews
                    WHERE status = 'VISIBLE'::review_status
                    GROUP BY product_id
                ) rv ON rv.product_id = p.id
                """ + whereClause + """
                GROUP BY
                    p.id,
                    p.name,
                    p.slug,
                    c.name,
                    p.created_at,
                    rv.avg_rating,
                    rv.review_count
                """ + resolveOrderBy(queryParam) + """
                LIMIT :limit OFFSET :offset
                """;

        List<ProductSummaryProjection> products = jdbcTemplate.query(mainSql, params, this::mapRowToProductSummary);

        SearchProductsProjection.Facets facets = buildFacets(products);

        return SearchProductsProjection.builder()
                .products(products)
                .total(total)
                .page(page + 1)
                .pageSize(size)
                .facets(facets)
                .build();
    }

    @Override
    public List<ProductSummaryProjection> findTopProducts(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    c.name AS category_name,
                    MIN(pv.price) AS product_price,
                    (
                        SELECT pi.image_url
                        FROM product_images pi
                        WHERE pi.product_id = p.id
                        ORDER BY pi.position ASC
                        LIMIT 1
                    ) AS product_image,
                    COALESCE(rv.avg_rating, 0) AS product_rating,
                    COALESCE(rv.review_count, 0) AS product_rating_count
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN product_variants pv ON pv.product_id = p.id
                LEFT JOIN (
                    SELECT product_id, AVG(rating) AS avg_rating, COUNT(id) AS review_count
                    FROM reviews
                    WHERE status = 'VISIBLE'::review_status
                    GROUP BY product_id
                ) rv ON rv.product_id = p.id
                WHERE p.product_status = 'ACTIVE'
                GROUP BY
                    p.id,
                    p.name,
                    p.slug,
                    c.name,
                    p.created_at,
                    rv.avg_rating,
                    rv.review_count
                ORDER BY p.created_at DESC
                LIMIT :limit
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("limit", limit),
                this::mapRowToProductSummary);
    }

    private void appendSearchFilter(StringBuilder whereClause, Map<String, Object> params, SearchProductsQuery query) {
        if (query.getQ() == null || query.getQ().isBlank()) {
            return;
        }

        whereClause.append("""
                AND (
                    LOWER(p.name) LIKE :q
                    OR LOWER(p.description) LIKE :q
                )
                """);

        params.put("q", "%" + query.getQ().trim().toLowerCase() + "%");
    }

    private void appendStatusFilter(StringBuilder whereClause, Map<String, Object> params, SearchProductsQuery query) {
        String status = normalizeText(query.getStatus(), "ACTIVE").toUpperCase();

        whereClause.append(" AND p.product_status = :status ");
        params.put("status", status);
    }

    private void appendCategoryFilter(StringBuilder whereClause, Map<String, Object> params,
            SearchProductsQuery query) {
        if (query.getCategory() == null || query.getCategory().isBlank()) {
            return;
        }

        UUID categoryId = resolveCategoryId(query.getCategory());

        if (categoryId == null) {
            whereClause.append(" AND 1 = 0 ");
            return;
        }

        // Include the category itself AND all its child categories
        List<UUID> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);

        String childSql = """
                SELECT id FROM categories WHERE parent_id = :parentId
                """;
        List<UUID> childIds = jdbcTemplate.query(
                childSql,
                Map.of("parentId", categoryId),
                (rs, rowNum) -> (UUID) rs.getObject("id"));
        categoryIds.addAll(childIds);

        whereClause.append(" AND p.category_id IN (:categoryIds) ");
        params.put("categoryIds", categoryIds);
    }

    private UUID resolveCategoryId(String rawCategory) {
        String category = rawCategory.trim();

        try {
            return UUID.fromString(category);
        } catch (IllegalArgumentException ignored) {
            String sql = """
                    SELECT id
                    FROM categories
                    WHERE LOWER(slug) = :slug
                    LIMIT 1
                    """;

            List<UUID> ids = jdbcTemplate.query(
                    sql,
                    Map.of("slug", category.toLowerCase()),
                    (rs, rowNum) -> (UUID) rs.getObject("id"));

            return ids.isEmpty() ? null : ids.get(0);
        }
    }

    private void appendVariantFilters(StringBuilder whereClause, Map<String, Object> params,
            SearchProductsQuery query) {
        if (query.getColors() != null && !query.getColors().isEmpty()) {
            whereClause.append("""
                    AND EXISTS (
                        SELECT 1
                        FROM product_variants pv_color
                        WHERE pv_color.product_id = p.id
                        AND LOWER(pv_color.color) IN (:colors)
                    )
                    """);

            params.put("colors", normalizeList(query.getColors()));
        }

        if (query.getMaterials() != null && !query.getMaterials().isEmpty()) {
            whereClause.append("""
                    AND EXISTS (
                        SELECT 1
                        FROM product_variants pv_material
                        WHERE pv_material.product_id = p.id
                        AND LOWER(pv_material.material) IN (:materials)
                    )
                    """);

            params.put("materials", normalizeList(query.getMaterials()));
        }
    }

    private void appendPriceFilters(StringBuilder whereClause, Map<String, Object> params, SearchProductsQuery query) {
        if (query.getPriceBands() != null && !query.getPriceBands().isEmpty()) {
            List<String> conditions = new ArrayList<>();

            for (String band : query.getPriceBands()) {
                switch (band) {
                    case "lt5m" -> conditions.add("pv_price.price < 5000000");
                    case "5-15m" -> conditions.add("(pv_price.price >= 5000000 AND pv_price.price <= 15000000)");
                    case "15-30m" -> conditions.add("(pv_price.price >= 15000000 AND pv_price.price <= 30000000)");
                    case "gt30m" -> conditions.add("pv_price.price > 30000000");
                    default -> {
                    }
                }
            }

            if (!conditions.isEmpty()) {
                whereClause.append("""
                        AND EXISTS (
                            SELECT 1
                            FROM product_variants pv_price
                            WHERE pv_price.product_id = p.id
                            AND (
                        """);

                whereClause.append(String.join(" OR ", conditions));
                whereClause.append(")) ");
            }
        }

        if (query.getPriceSliderPct() != null && !query.getPriceSliderPct().isEmpty()) {
            Double pct = query.getPriceSliderPct().get(0);

            if (pct != null && pct < 100) {
                double maxPrice = (pct / 100.0) * 50_000_000.0;

                whereClause.append("""
                        AND EXISTS (
                            SELECT 1
                            FROM product_variants pv_slider
                            WHERE pv_slider.product_id = p.id
                            AND pv_slider.price <= :maxPrice
                        )
                        """);

                params.put("maxPrice", maxPrice);
            }
        }
    }

    private void appendRatingFilters(StringBuilder whereClause, Map<String, Object> params, SearchProductsQuery query) {
        if (query.getMinStar() != null && query.getMinStar() >= 1 && query.getMinStar() <= 5) {
            whereClause.append("""
                    AND (
                        SELECT AVG(rating)
                        FROM reviews r
                        WHERE r.product_id = p.id
                        AND r.status = 'VISIBLE'::review_status
                    ) >= :minStar
                    """);
            params.put("minStar", query.getMinStar().doubleValue());
        }
    }

    private Long countProducts(StringBuilder whereClause, Map<String, Object> params) {
        String countSql = """
                SELECT COUNT(DISTINCT p.id)
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                """ + whereClause;

        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return total == null ? 0L : total;
    }

    private String resolveOrderBy(SearchProductsQuery query) {
        if (query.getSort() == null) {
            return " ORDER BY p.created_at DESC ";
        }

        return switch (query.getSort().trim().toLowerCase()) {
            case "newest" -> " ORDER BY p.created_at DESC ";
            case "price-asc" -> " ORDER BY MIN(pv.price) ASC NULLS LAST ";
            case "price-desc" -> " ORDER BY MIN(pv.price) DESC NULLS LAST ";
            case "rating", "popular" -> " ORDER BY p.created_at DESC ";
            default -> " ORDER BY p.created_at DESC ";
        };
    }

    private void hydrateProductDetail(ProductDetailProjection dto) {
        List<ProductDetailProjection.VariantDto> variants = fetchVariants(dto.getId());
        dto.setVariants(variants);

        if (!variants.isEmpty()) {
            dto.setPrice(variants.get(0).getPrice());
        }

        dto.setGallery(fetchGallery(dto.getId()));
    }

    private ProductSummaryProjection mapRowToProductSummary(ResultSet rs, int rowNum) throws SQLException {
        UUID id = (UUID) rs.getObject("product_id");

        String slug = normalizeText(rs.getString("product_slug"), id.toString());
        String name = normalizeText(rs.getString("product_name"), "Sản phẩm");
        String categoryName = normalizeText(rs.getString("category_name"), "Sản phẩm");

        Double price = getNullableDouble(rs, "product_price");
        if (price == null) {
            price = 0.0;
        }

        String imageUrl = normalizeText(rs.getString("product_image"), null);

        return ProductSummaryProjection.builder()
                .id(id)
                .slug(slug)
                .name(name)
                .categoryName(categoryName)
                .price(price)
                .oldPrice(price > 0 ? price * 1.2 : null)
                .image(imageUrl)
                .rating(getNullableDouble(rs, "product_rating"))
                .ratingCount(rs.getInt("product_rating_count"))
                .tags(List.of("new"))
                .build();
    }

    private ProductDetailProjection mapRowToProductDetail(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("product_id");
        UUID categoryId = (UUID) rs.getObject("category_id");

        String categoryName = normalizeText(rs.getString("category_name"), "Sản phẩm");
        String categorySlug = normalizeText(rs.getString("category_slug"), null);

        List<String> features = parseJsonList(rs.getString("product_features"));
        if (features.isEmpty()) {
            features = List.of(
                    "Thiết kế hiện đại",
                    "Chất liệu cao cấp",
                    "Bảo hành 12 tháng");
        }

        return ProductDetailProjection.builder()
                .id(id)
                .slug(normalizeText(rs.getString("product_slug"), id.toString()))
                .category(ProductDetailProjection.CategoryInfo.builder()
                        .id(categorySlug != null ? categorySlug : categoryId != null ? categoryId.toString() : null)
                        .label(categoryName)
                        .build())
                .name(normalizeText(rs.getString("product_name"), "Sản phẩm"))
                .description(normalizeText(rs.getString("product_description"), ""))
                .status(normalizeText(rs.getString("product_status"), "ACTIVE"))
                .rating(getNullableDouble(rs, "avg_rating"))
                .ratingCount(rs.getInt("review_count"))
                .tags(List.of("new", "sale"))
                .supports3d(rs.getBoolean("supports_3d"))
                .collection(normalizeText(rs.getString("collection_name"), null))
                .features(features)
                .price(0.0)
                .modelUrl(normalizeText(rs.getString("model_url"), ""))
                .roomTypeHint(categoryName)
                .build();
    }

    private List<ProductDetailProjection.VariantDto> fetchVariants(UUID productId) {
        String sql = """
                SELECT
                    id,
                    price,
                    stock_quantity,
                    weight,
                    length,
                    width,
                    height,
                    color,
                    material,
                    warranty
                FROM product_variants
                WHERE product_id = :productId
                ORDER BY price ASC
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("productId", productId),
                (rs, rowNum) -> ProductDetailProjection.VariantDto.builder()
                        .id((UUID) rs.getObject("id"))
                        .price(getNullableDouble(rs, "price"))
                        .stockQuantity(rs.getInt("stock_quantity"))
                        .weight(getNullableDouble(rs, "weight"))
                        .length(getNullableDouble(rs, "length"))
                        .width(getNullableDouble(rs, "width"))
                        .height(getNullableDouble(rs, "height"))
                        .color(normalizeText(rs.getString("color"), ""))
                        .material(normalizeText(rs.getString("material"), ""))
                        .warranty(normalizeText(rs.getString("warranty"), ""))
                        .build());
    }

    private List<String> fetchGallery(UUID productId) {
        String sql = """
                SELECT image_url
                FROM product_images
                WHERE product_id = :productId
                ORDER BY position ASC
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("productId", productId),
                (rs, rowNum) -> rs.getString("image_url"));
    }

    private SearchProductsProjection.Facets buildFacets(List<ProductSummaryProjection> products) {
        Map<String, Long> categoryCounts = products.stream()
                .filter(product -> product.getCategoryName() != null)
                .collect(Collectors.groupingBy(
                        ProductSummaryProjection::getCategoryName,
                        Collectors.counting()));

        List<SearchProductsProjection.CategoryFacet> categories = categoryCounts.entrySet().stream()
                .map(entry -> SearchProductsProjection.CategoryFacet.builder()
                        .id(toSlug(entry.getKey()))
                        .slug(toSlug(entry.getKey()))
                        .label(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();

        List<SearchProductsProjection.MaterialFacet> materials = fetchMaterialFacets();
        List<SearchProductsProjection.ColorFacet> colors = fetchColorFacets();
        Map<Integer, Long> ratings = buildRatingFacets(products);

        return SearchProductsProjection.Facets.builder()
                .categories(categories)
                .materials(materials)
                .colors(colors)
                .ratings(ratings)
                .build();
    }

    private Map<Integer, Long> buildRatingFacets(List<ProductSummaryProjection> products) {
        Map<Integer, Long> facets = new HashMap<>();

        for (int star = 5; star >= 1; star--) {
            final int minStar = star;
            long count = products.stream()
                    .filter(p -> p.getRating() != null && p.getRating() >= minStar)
                    .count();

            facets.put(star, count);
        }

        return facets;
    }

    private List<SearchProductsProjection.MaterialFacet> fetchMaterialFacets() {
        String sql = """
                SELECT DISTINCT material
                FROM product_variants
                WHERE material IS NOT NULL
                AND material != ''
                ORDER BY material ASC
                """;

        return jdbcTemplate.query(
                sql,
                Map.of(),
                (rs, rowNum) -> {
                    String material = rs.getString("material");

                    return SearchProductsProjection.MaterialFacet.builder()
                            .id(material.toLowerCase())
                            .label(capitalize(material))
                            .build();
                });
    }

    private List<SearchProductsProjection.ColorFacet> fetchColorFacets() {
        String sql = """
                SELECT DISTINCT color
                FROM product_variants
                WHERE color IS NOT NULL
                AND color != ''
                ORDER BY color ASC
                """;

        return jdbcTemplate.query(
                sql,
                Map.of(),
                (rs, rowNum) -> {
                    String color = rs.getString("color");

                    return SearchProductsProjection.ColorFacet.builder()
                            .id(color.toLowerCase())
                            .label(capitalize(color))
                            .hex(getColorHex(color))
                            .build();
                });
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (Exception e) {
            log.warn("Failed to parse JSON list from value: {}", json, e);
            return new ArrayList<>();
        }
    }

    private List<String> normalizeList(List<String> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .toList();
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim();
    }

    private Double getNullableDouble(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = value.trim();
        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
    }

    private String toSlug(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.trim()
                .toLowerCase()
                .replace(" ", "-");
    }

    private String getColorHex(String color) {
        if (color == null) {
            return "#CCCCCC";
        }

        return switch (color.trim().toLowerCase()) {
            case "brown", "walnut", "walnut-brown" -> "#8B4513";
            case "natural" -> "#F5DEB3";
            case "white" -> "#FFFFFF";
            case "black" -> "#000000";
            case "grey", "gray" -> "#808080";
            case "beige", "cream" -> "#F5F5DC";
            case "gold" -> "#D4AF37";
            case "green" -> "#2E7D32";
            case "blue" -> "#1565C0";
            default -> "#CCCCCC";
        };
    }
}
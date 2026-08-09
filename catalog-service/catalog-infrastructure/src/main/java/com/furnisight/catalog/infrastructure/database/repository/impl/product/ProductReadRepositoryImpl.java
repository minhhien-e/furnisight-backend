package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.domain.valueobjects.product.VariantSpecifications;
import com.furnisight.catalog.infrastructure.integration.remote.RemoteMediaUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductReadRepositoryImpl implements ProductReadRepository {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private VariantSpecifications parseSpecifications(Object rawJson) {
        if (rawJson == null) return null;
        String json = rawJson.toString();
        if (json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, VariantSpecifications.class);
        } catch (Exception e) {
            log.warn("Failed to parse variant specifications JSON", e);
            return null;
        }
    }

    private List<String> parseFeatures(Object rawJson) {
        if (rawJson == null) return List.of();
        String json = rawJson.toString();
        if (json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse variant features JSON", e);
            return List.of();
        }
    }

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RemoteMediaUrlResolver mediaUrlResolver;

    @Override
    public Optional<ProductResponse> findProductDetailBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.category_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    p.sku AS product_sku,
                    p.description AS product_description,
                    p.product_status,
                    p.features AS product_features,
                    p.image_url AS product_image_url,
                    p.image_media_id AS product_media_id,
                    (SELECT pv.model_media_id FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true ORDER BY pv.price ASC LIMIT 1) AS model_media_id,
                    (SELECT pv.model_url FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true ORDER BY pv.price ASC LIMIT 1) AS model_url,
                    EXISTS(SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true) AS supports_3d,
                    p.sold_count,
                    c.name AS category_name,
                    c.slug AS category_slug,
                    pc.name AS parent_category_name,
                    pc.slug AS parent_category_slug,
                    p.rating AS avg_rating,
                    p.rating_count AS review_count
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN categories pc ON c.parent_id = pc.id
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

                    ProductResponse dto = mapRowToProductDetail(rs);
                    hydrateProductDetail(dto);
                    return Optional.of(dto);
                });
    }

    @Override
    public Optional<ProductResponse> findProductDetailById(UUID productId) {
        if (productId == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.category_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    p.sku AS product_sku,
                    p.description AS product_description,
                    p.product_status,
                    p.features AS product_features,
                    p.image_url AS product_image_url,
                    p.image_media_id AS product_media_id,
                    (SELECT pv.model_media_id FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true ORDER BY pv.price ASC LIMIT 1) AS model_media_id,
                    (SELECT pv.model_url FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true ORDER BY pv.price ASC LIMIT 1) AS model_url,
                    EXISTS(SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true) AS supports_3d,
                    p.sold_count,
                    c.name AS category_name,
                    c.slug AS category_slug,
                    pc.name AS parent_category_name,
                    pc.slug AS parent_category_slug,
                    p.rating AS avg_rating,
                    p.rating_count AS review_count
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN categories pc ON c.parent_id = pc.id
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

                    ProductResponse dto = mapRowToProductDetail(rs);
                    hydrateProductDetail(dto);
                    return Optional.of(dto);
                });
    }

    @Override
    public List<ProductResponse> findProductDetailsByIds(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.category_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    p.sku AS product_sku,
                    p.description AS product_description,
                    p.product_status,
                    p.features AS product_features,
                    p.image_url AS product_image_url,
                    p.image_media_id AS product_media_id,
                    (SELECT pv.model_media_id FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true ORDER BY pv.price ASC LIMIT 1) AS model_media_id,
                    (SELECT pv.model_url FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true ORDER BY pv.price ASC LIMIT 1) AS model_url,
                    EXISTS(SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true) AS supports_3d,
                    p.sold_count,
                    c.name AS category_name,
                    c.slug AS category_slug,
                    pc.name AS parent_category_name,
                    pc.slug AS parent_category_slug,
                    p.rating AS avg_rating,
                    p.rating_count AS review_count
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN categories pc ON c.parent_id = pc.id
                WHERE CAST(p.id AS text) IN (:productIdsText)
                """;

        List<ProductResponse> products = jdbcTemplate.query(
                sql,
                Map.of("productIdsText", productIds.stream().map(UUID::toString).toList()),
                (rs, rowNum) -> mapRowToProductDetail(rs));

        if (!products.isEmpty()) {
            Map<UUID, List<ProductResponse.VariantDto>> variantsMap = fetchVariantsInBatch(productIds);
            Map<UUID, List<String>> galleryMap = fetchGalleryInBatch(productIds);

            for (ProductResponse dto : products) {
                List<ProductResponse.VariantDto> variants = variantsMap.getOrDefault(dto.getId(), new ArrayList<>());
                dto.setVariants(variants);

                if (!variants.isEmpty()) {
                    dto.setPrice(variants.get(0).getPrice());
                }

                dto.setGallery(galleryMap.getOrDefault(dto.getId(), new ArrayList<>()));
            }
        }

        return products;
    }

    @Override
    public List<ProductResponse.ProductStockDto> findStockByVariantIds(List<UUID> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT
                    v.id,
                    v.product_id,
                    v.stock_quantity
                FROM product_variants v
                WHERE CAST(v.id AS text) IN (:variantIdsText)
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("variantIdsText", variantIds.stream().map(UUID::toString).toList()),
                (rs, rowNum) -> ProductResponse.ProductStockDto.builder()
                        .variantId((UUID) rs.getObject("id"))
                        .productId((UUID) rs.getObject("product_id"))
                        .stockQuantity(rs.getInt("stock_quantity"))
                        .build()
        );
    }

    @Override
    public PageResponse<ProductResponse> searchProducts(SearchProductsQuery queryParam) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        appendSearchFilter(whereClause, params, queryParam);
        appendStatusFilter(whereClause, params, queryParam);
        appendCategoryFilter(whereClause, params, queryParam);
        appendRoomTypeFilter(whereClause, params, queryParam);
        appendVariantFilters(whereClause, params, queryParam);
        appendRatingFilters(whereClause, params, queryParam);

        Long total = countProducts(whereClause, params);

        int page = Math.max(queryParam.getPage(), 0);
        int size = queryParam.getSize() > 0 ? queryParam.getSize() : 24;
        int offset = page * size;

        params.put("limit", size);
        params.put("offset", offset);

        String mainSql = """
                WITH paged_products AS (
                    SELECT p.id, p.name, p.slug, p.sold_count, p.rating, p.rating_count, p.created_at, p.category_id, p.image_url, p.image_media_id, p.base_price
                    """ + (queryParam.getSort() != null && queryParam.getSort().toLowerCase().contains("price") 
                           ? ", COALESCE((SELECT MIN(pv.price) FROM product_variants pv WHERE pv.product_id = p.id), p.base_price) AS product_price " 
                           : "") + """
                    FROM products p
                    """ + whereClause + resolveOrderBy(queryParam) + """
                    LIMIT :limit OFFSET :offset
                )
                SELECT
                    pp.id AS product_id,
                    pp.name AS product_name,
                    pp.slug AS product_slug,
                    pp.sold_count AS product_sold_count,
                    v3d.model_url,
                    COALESCE(v3d.supports_3d, false) AS supports_3d,
                    c.name AS category_name,
                    COALESCE((SELECT MIN(pv.price) FROM product_variants pv WHERE pv.product_id = pp.id), pp.base_price) AS product_price,
                    pp.image_url AS product_image_url,
                    pp.image_media_id AS product_media_id,
                    pp.rating AS product_rating,
                    pp.rating_count AS product_rating_count,
                    pp.created_at
                FROM paged_products pp
                LEFT JOIN categories c ON pp.category_id = c.id
                LEFT JOIN LATERAL (
                    SELECT pv.model_url, pv.supports_3d
                    FROM product_variants pv
                    WHERE pv.product_id = pp.id AND pv.supports_3d = true
                    ORDER BY pv.price ASC
                    LIMIT 1
                ) v3d ON TRUE
                """ + resolveOrderByPagedProducts(queryParam) + """
                """;

        List<ProductResponse> products = jdbcTemplate.query(mainSql, params, this::mapRowToProductSummary);
        if (!products.isEmpty()) {
            List<UUID> productIds = products.stream().map(ProductResponse::getId).toList();
            Map<UUID, List<ProductResponse.VariantDto>> variantsMap = fetchVariantsInBatch(productIds);
            products.forEach(p -> p.setVariants(variantsMap.getOrDefault(p.getId(), new ArrayList<>())));
        }
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) total / size);

        return new PageResponse<>(products, totalPages, total, page + 1, size);
    }

    @Override
    public List<ProductResponse> findRecommendedProducts(
            String categorySlug, String status, int limit) {
        if (categorySlug == null || categorySlug.isBlank() || limit <= 0) {
            return List.of();
        }

        String sql = """
                WITH RECURSIVE category_tree AS (
                    SELECT id
                    FROM categories
                    WHERE LOWER(slug) = :categorySlug

                    UNION

                    SELECT child.id
                    FROM categories child
                    JOIN category_tree parent ON child.parent_id = parent.id
                )
                SELECT
                    p.id AS product_id,
                    p.slug AS product_slug,
                    p.name AS product_name,
                    c.name AS category_name,
                    cheapest_variant.id AS default_variant_id,
                    cheapest_variant.price AS product_price,
                    cheapest_variant.model_url,
                    cheapest_variant.supports_3d,
                    p.sold_count,
                    p.features AS product_features,
                    p.image_url AS product_image_url,
                    p.image_media_id AS product_media_id,
                    p.rating AS product_rating,
                    p.rating_count AS product_rating_count
                FROM products p
                JOIN categories c ON c.id = p.category_id
                LEFT JOIN LATERAL (
                    SELECT pv.id, pv.price, pv.model_url, pv.supports_3d
                    FROM product_variants pv
                    WHERE pv.product_id = p.id
                      AND pv.price IS NOT NULL
                    ORDER BY pv.price ASC, pv.id ASC
                    LIMIT 1
                ) cheapest_variant ON TRUE
                WHERE p.category_id IN (SELECT id FROM category_tree)
                  AND p.product_status = :status
                ORDER BY p.created_at DESC
                LIMIT :limit
                """;

        List<ProductResponse> products = jdbcTemplate.query(
                sql,
                Map.of(
                        "categorySlug", categorySlug.trim().toLowerCase(),
                        "status", normalizeText(status, "ACTIVE").toUpperCase(),
                        "limit", limit),
                (rs, rowNum) -> ProductResponse.builder()
                        .id((UUID) rs.getObject("product_id"))
                        .slug(normalizeText(rs.getString("product_slug"), ""))
                        .name(normalizeText(rs.getString("product_name"), "Sản phẩm"))
                        .categoryName(normalizeText(rs.getString("category_name"), "Sản phẩm"))
                        .defaultVariantId((UUID) rs.getObject("default_variant_id"))
                        .price(getNullableDouble(rs, "product_price"))
                        .image(normalizeText(rs.getString("product_image"), null))
                        .rating(getNullableDouble(rs, "product_rating"))
                        .ratingCount(rs.getInt("product_rating_count"))
                        .soldCount(rs.getInt("sold_count"))
                        .build());
                        
        if (!products.isEmpty()) {
            List<UUID> productIds = products.stream().map(ProductResponse::getId).toList();
            Map<UUID, List<ProductResponse.VariantDto>> variantsMap = fetchVariantsInBatch(productIds);
            products.forEach(p -> p.setVariants(variantsMap.getOrDefault(p.getId(), new ArrayList<>())));
        }
        return products;
    }

    @Override
    public List<ProductResponse> findTopProducts(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    p.sold_count AS product_sold_count,
                    c.name AS category_name,
                    v3d.model_url,
                    COALESCE(v3d.supports_3d, false) AS supports_3d,
                    COALESCE((SELECT MIN(pv.price) FROM product_variants pv WHERE pv.product_id = p.id), p.base_price) AS product_price,
                    p.image_url AS product_image_url,
                    p.image_media_id AS product_media_id,
                    p.rating AS product_rating,
                    p.rating_count AS product_rating_count
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN LATERAL (
                    SELECT pv.model_url, pv.supports_3d
                    FROM product_variants pv
                    WHERE pv.product_id = p.id AND pv.supports_3d = true
                    ORDER BY pv.price ASC
                    LIMIT 1
                ) v3d ON TRUE
                WHERE p.product_status = 'ACTIVE'
                ORDER BY p.created_at DESC
                LIMIT :limit
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("limit", limit),
                this::mapRowToProductSummary);
    }

    @Override
    public List<ProductResponse> findAdminProducts(String query, String status, String category, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        String whereClause = buildAdminProductWhereClause(query, status, category, params);
        params.put("limit", Math.max(size, 1));
        params.put("offset", Math.max(page, 0) * Math.max(size, 1));

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.name AS product_name,
                    p.slug AS product_slug,
                    p.sku AS product_sku,
                    p.product_status,
                    (SELECT pv.model_media_id FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true ORDER BY pv.price ASC LIMIT 1) AS model_media_id,
                    (SELECT pv.model_url FROM product_variants pv WHERE pv.product_id = p.id AND pv.supports_3d = true ORDER BY pv.price ASC LIMIT 1) AS model_url,
                    c.name AS category_name,
                    COALESCE((SELECT MIN(pv.price) FROM product_variants pv WHERE pv.product_id = p.id), p.base_price) AS product_price,
                    (SELECT COALESCE(SUM(pv.stock_quantity), 0) FROM product_variants pv WHERE pv.product_id = p.id) AS product_stock
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                """ + whereClause + """
                ORDER BY p.created_at DESC
                LIMIT :limit OFFSET :offset
                """;

        return jdbcTemplate.query(sql, params, this::mapRowToAdminProduct);
    }

    @Override
    public long countAdminProducts(String query, String status, String category) {
        Map<String, Object> params = new HashMap<>();
        String whereClause = buildAdminProductWhereClause(query, status, category, params);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(p.id) FROM products p LEFT JOIN categories c ON p.category_id = c.id " + whereClause,
                params,
                Long.class);
        return total == null ? 0L : total;
    }

    @Override
    public long countProductsByStatus(String status) {
        if (status == null || status.isBlank()) {
            return 0L;
        }
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE product_status = :status",
                Map.of("status", status.trim().toUpperCase()),
                Long.class);
        return total == null ? 0L : total;
    }

    @Override
    public long countLowStockProducts() {
        Long total = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM (
                    SELECT p.id
                    FROM products p
                    JOIN product_variants pv ON pv.product_id = p.id
                    GROUP BY p.id
                    HAVING BOOL_OR(pv.stock_quantity > 0
                        AND pv.stock_quantity <= pv.low_stock_threshold)
                ) stock_view
                """,
                Map.of(),
                Long.class);
        return total == null ? 0L : total;
    }

    @Override
    public long countOutOfStockProducts() {
        Long total = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM (
                    SELECT p.id, COALESCE(SUM(pv.stock_quantity), 0) AS stock
                    FROM products p
                    LEFT JOIN product_variants pv ON pv.product_id = p.id
                    GROUP BY p.id
                ) stock_view
                WHERE stock <= 0
                """,
                Map.of(),
                Long.class);
        return total == null ? 0L : total;
    }

    @Override
    public List<ProductResponse> findLowStockProducts(int limit) {
        String sql = """
                SELECT
                    p.id AS product_id,
                    p.name AS product_name,
                    c.name AS category_name,
                    COALESCE(SUM(pv.stock_quantity), 0) AS product_stock
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                LEFT JOIN product_variants pv ON pv.product_id = p.id
                GROUP BY p.id, p.name, c.name
                HAVING BOOL_OR(pv.stock_quantity > 0
                    AND pv.stock_quantity <= pv.low_stock_threshold)
                ORDER BY product_stock ASC, p.name ASC
                LIMIT :limit
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("limit", Math.max(limit, 1)),
                (rs, rowNum) -> ProductResponse.builder()
                        .id((UUID) rs.getObject("product_id"))
                        .name(normalizeText(rs.getString("product_name"), "Sản phẩm"))
                        .categoryName(normalizeText(rs.getString("category_name"), "Sản phẩm"))
                        .stock(rs.getInt("product_stock"))
                        .build());
    }

    private void appendSearchFilter(StringBuilder whereClause, Map<String, Object> params, SearchProductsQuery query) {
        if (query.getQ() == null || query.getQ().isBlank()) {
            return;
        }

        whereClause.append("""
                AND (
                    LOWER(p.name) LIKE :q
                    OR LOWER(p.description) LIKE :q
                    OR LOWER(p.sku) LIKE :q
                    OR p.id IN (
                        SELECT pv.product_id FROM product_variants pv
                        WHERE LOWER(pv.sku) LIKE :q
                    )
                )
                """);

        params.put("q", "%" + query.getQ().trim().toLowerCase() + "%");
    }

    private String buildAdminProductWhereClause(String query, String status, String category, Map<String, Object> params) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1 = 1 ");
        if (query != null && !query.isBlank()) {
            whereClause.append("""
                     AND (
                         LOWER(p.name) LIKE :adminQuery 
                         OR LOWER(p.slug) LIKE :adminQuery
                         OR LOWER(p.sku) LIKE :adminQuery
                         OR p.id IN (
                             SELECT pv.product_id FROM product_variants pv
                             WHERE LOWER(pv.sku) LIKE :adminQuery
                         )
                     ) 
                    """);
            params.put("adminQuery", "%" + query.trim().toLowerCase() + "%");
        }
        if (status != null && !status.isBlank()) {
            whereClause.append(" AND p.product_status = :adminStatus ");
            params.put("adminStatus", status.trim().toUpperCase());
        }
        if (category != null && !category.isBlank()) {
            whereClause.append(" AND (LOWER(c.name) = :adminCategory OR LOWER(c.slug) = :adminCategory OR CAST(c.id AS text) = :adminCategory) ");
            params.put("adminCategory", category.trim().toLowerCase());
        }
        return whereClause.toString();
    }

    private ProductResponse mapRowToAdminProduct(ResultSet rs, int rowNum) throws SQLException {
        UUID id = (UUID) rs.getObject("product_id");
        String slug = normalizeText(rs.getString("product_slug"), id.toString());
        String sku = normalizeText(rs.getString("product_sku"), slug);
        return ProductResponse.builder()
                .id(id)
                .name(normalizeText(rs.getString("product_name"), "Sản phẩm"))
                .slug(slug)
                .sku(sku)
                .categoryName(normalizeText(rs.getString("category_name"), "Sản phẩm"))
                .price(getNullableDouble(rs, "product_price") == null ? 0D : getNullableDouble(rs, "product_price"))
                .stock(rs.getInt("product_stock"))
                .status(normalizeText(rs.getString("product_status"), "ACTIVE"))
                .imageUrls(fetchGallery(id))
                .build();
    }

    private void appendStatusFilter(StringBuilder whereClause, Map<String, Object> params, SearchProductsQuery query) {
        String status = normalizeText(query.getStatus(), "ACTIVE").toUpperCase();

        whereClause.append(" AND p.product_status = :status ");
        params.put("status", status);
    }

    private void appendRoomTypeFilter(StringBuilder whereClause, Map<String, Object> params,
            SearchProductsQuery query) {
        if (query.getRoomType() == null || query.getRoomType().isBlank() || "all".equalsIgnoreCase(query.getRoomType().trim())) {
            return;
        }

        String roomType = query.getRoomType().trim().toLowerCase();

        String sql = """
                SELECT c.id FROM categories c
                JOIN room_types rt ON c.room_type_id = rt.id
                WHERE LOWER(rt.slug) = :roomType OR CAST(rt.id AS text) = :roomType
                """;
        List<UUID> categoryIds = jdbcTemplate.query(
                sql,
                Map.of("roomType", roomType),
                (rs, rowNum) -> (UUID) rs.getObject("id"));

        if (categoryIds == null || categoryIds.isEmpty()) {
            whereClause.append(" AND 1 = 0 ");
            return;
        }

        whereClause.append(" AND p.category_id IN (:roomTypeCatIds) ");
        params.put("roomTypeCatIds", categoryIds);
    }

    private void appendCategoryFilter(StringBuilder whereClause, Map<String, Object> params,
            SearchProductsQuery query) {
        if (query.getCategory() == null || query.getCategory().isBlank() || "all".equalsIgnoreCase(query.getCategory().trim())) {
            return;
        }

        List<UUID> categoryIds = resolveCategoryIds(query.getCategory());

        if (categoryIds == null || categoryIds.isEmpty()) {
            whereClause.append(" AND 1 = 0 ");
            return;
        }

        whereClause.append(" AND p.category_id IN (:categoryIds) ");
        params.put("categoryIds", categoryIds);
    }

    private List<UUID> resolveCategoryIds(String rawCategory) {
        String category = rawCategory.trim();

        // 1. Try RoomType slug or RoomType UUID matching
        String roomTypeSql = """
                SELECT c.id FROM categories c
                JOIN room_types rt ON c.room_type_id = rt.id
                WHERE LOWER(rt.slug) = :category OR CAST(rt.id AS text) = :category
                """;
        List<UUID> roomTypeCatIds = jdbcTemplate.query(
                roomTypeSql,
                Map.of("category", category.toLowerCase()),
                (rs, rowNum) -> (UUID) rs.getObject("id"));

        if (!roomTypeCatIds.isEmpty()) {
            return roomTypeCatIds;
        }

        // 2. Try Category slug or Category UUID matching
        UUID categoryId = null;
        try {
            categoryId = UUID.fromString(category);
        } catch (IllegalArgumentException ignored) {
            String sql = "SELECT id FROM categories WHERE LOWER(slug) = :slug LIMIT 1";
            List<UUID> ids = jdbcTemplate.query(sql, Map.of("slug", category.toLowerCase()), (rs, rowNum) -> (UUID) rs.getObject("id"));
            if (!ids.isEmpty()) {
                categoryId = ids.get(0);
            }
        }

        if (categoryId == null) {
            return List.of();
        }

        List<UUID> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);

        String childSql = "SELECT id FROM categories WHERE parent_id = :parentId";
        List<UUID> childIds = jdbcTemplate.query(
                childSql,
                Map.of("parentId", categoryId),
                (rs, rowNum) -> (UUID) rs.getObject("id"));
        categoryIds.addAll(childIds);

        return categoryIds;
    }

    private void appendVariantFilters(StringBuilder whereClause, Map<String, Object> params,
            SearchProductsQuery query) {
        boolean hasColors = query.getColors() != null && !query.getColors().isEmpty();
        boolean hasMaterials = query.getMaterials() != null && !query.getMaterials().isEmpty();
        List<String> priceConditions = new ArrayList<>();

        if (query.getPriceBands() != null && !query.getPriceBands().isEmpty()) {
            for (String band : query.getPriceBands()) {
                switch (band) {
                    case "lt5m" -> priceConditions.add("pv_filter.price < 5000000");
                    case "5-15m" -> priceConditions.add("(pv_filter.price >= 5000000 AND pv_filter.price <= 15000000)");
                    case "15-30m" -> priceConditions.add("(pv_filter.price >= 15000000 AND pv_filter.price <= 30000000)");
                    case "gt30m" -> priceConditions.add("pv_filter.price > 30000000");
                    default -> {
                    }
                }
            }
        }

        Double sliderMaxPrice = null;
        if (query.getPriceSliderPct() != null && !query.getPriceSliderPct().isEmpty()) {
            Double pct = query.getPriceSliderPct().get(0);
            if (pct != null && pct < 100) {
                sliderMaxPrice = (pct / 100.0) * 50_000_000.0;
            }
        }

        if (!hasColors && !hasMaterials && priceConditions.isEmpty() && sliderMaxPrice == null) {
            return;
        }

        whereClause.append("""
                AND EXISTS (
                    SELECT 1
                    FROM product_variants pv_filter
                    WHERE pv_filter.product_id = p.id
                """);

        if (hasColors) {
            whereClause.append(" AND LOWER(pv_filter.color) IN (:colors) ");
            params.put("colors", normalizeList(query.getColors()));
        }

        if (hasMaterials) {
            whereClause.append(" AND LOWER(pv_filter.material) IN (:materials) ");
            params.put("materials", normalizeList(query.getMaterials()));
        }

        if (!priceConditions.isEmpty()) {
            whereClause.append(" AND (");
            whereClause.append(String.join(" OR ", priceConditions));
            whereClause.append(") ");
        }

        if (sliderMaxPrice != null) {
            whereClause.append(" AND pv_filter.price <= :maxPrice ");
            params.put("maxPrice", sliderMaxPrice);
        }

        whereClause.append(") ");
    }

    private void appendRatingFilters(StringBuilder whereClause, Map<String, Object> params, SearchProductsQuery query) {
        if (query.getMinStar() != null && query.getMinStar() >= 1 && query.getMinStar() <= 5) {
            whereClause.append(" AND p.rating >= :minStar ");
            params.put("minStar", query.getMinStar().doubleValue());
        }
    }

    private Long countProducts(StringBuilder whereClause, Map<String, Object> params) {
        String countSql = """
                SELECT COUNT(p.id)
                FROM products p
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
            case "price-asc" -> " ORDER BY product_price ASC NULLS LAST, p.id ASC ";
            case "price-desc" -> " ORDER BY product_price DESC NULLS LAST, p.id ASC ";
            case "rating" -> " ORDER BY p.rating DESC NULLS LAST, p.rating_count DESC, p.id ASC ";
            case "popular" -> " ORDER BY p.sold_count DESC NULLS LAST, p.id ASC ";
            default -> " ORDER BY p.created_at DESC ";
        };
    }

    private String resolveOrderByPagedProducts(SearchProductsQuery query) {
        if (query.getSort() == null) {
            return " ORDER BY pp.created_at DESC ";
        }

        return switch (query.getSort().trim().toLowerCase()) {
            case "newest" -> " ORDER BY pp.created_at DESC ";
            case "price-asc" -> " ORDER BY product_price ASC NULLS LAST, pp.id ASC ";
            case "price-desc" -> " ORDER BY product_price DESC NULLS LAST, pp.id ASC ";
            case "rating" -> " ORDER BY pp.rating DESC NULLS LAST, pp.rating_count DESC, pp.id ASC ";
            case "popular" -> " ORDER BY pp.sold_count DESC NULLS LAST, pp.id ASC ";
            default -> " ORDER BY pp.created_at DESC ";
        };
    }

    private void hydrateProductDetail(ProductResponse dto) {
        List<ProductResponse.VariantDto> variants = fetchVariants(dto.getId());
        dto.setVariants(variants);

        if (!variants.isEmpty()) {
            dto.setPrice(variants.get(0).getPrice());
        }

        List<String> gallery = fetchGallery(dto.getId());
        dto.setGallery(gallery);
        // Also set imageUrls if not already populated from the main query
        if ((dto.getImageUrls() == null || dto.getImageUrls().isEmpty()) && !gallery.isEmpty()) {
            dto.setImageUrls(new ArrayList<>(gallery));
        }
    }

    private ProductResponse mapRowToProductSummary(ResultSet rs, int rowNum) throws SQLException {
        UUID id = (UUID) rs.getObject("product_id");

        String slug = normalizeText(rs.getString("product_slug"), id.toString());
        String name = normalizeText(rs.getString("product_name"), "Sản phẩm");
        String categoryName = normalizeText(rs.getString("category_name"), "Sản phẩm");

        Double price = getNullableDouble(rs, "product_price");
        if (price == null) {
            price = 0.0;
        }

        String imageUrl = normalizeText(rs.getString("product_image_url"), null);
        UUID mediaId = rs.getObject("product_media_id", UUID.class);
        if (imageUrl == null && mediaId != null) {
            imageUrl = mediaUrlResolver.resolveUrl(mediaId).orElse(null);
        }

        Boolean supports3d = rs.getBoolean("supports_3d");

        return ProductResponse.builder()
                .id(id)
                .slug(slug)
                .name(name)
                .categoryName(categoryName)
                .price(price)
                .image(imageUrl)
                .supports3d(supports3d)
                .rating(getNullableDouble(rs, "product_rating"))
                .ratingCount(rs.getInt("product_rating_count"))
                .soldCount(rs.getInt("product_sold_count"))
                .build();
    }

    private ProductResponse mapRowToProductDetail(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("product_id");
        UUID categoryId = (UUID) rs.getObject("category_id");

        String categoryName = normalizeText(rs.getString("category_name"), "Sản phẩm");
        String categorySlug = normalizeText(rs.getString("category_slug"), null);
        String parentCategoryName = normalizeText(rs.getString("parent_category_name"), null);
        String parentCategorySlug = normalizeText(rs.getString("parent_category_slug"), null);
        String categoryPath = parentCategorySlug != null && categorySlug != null
                ? parentCategorySlug + "/" + categorySlug
                : categorySlug;

        List<String> features = parseJsonList(rs.getString("product_features"));
        if (features.isEmpty()) {
            features = List.of(
                    "Thiết kế hiện đại",
                    "Chất liệu cao cấp",
                    "Bảo hành 12 tháng");
        }

        String productImageUrl = normalizeText(rs.getString("product_image_url"), null);
        UUID productMediaId = null;
        try { productMediaId = rs.getObject("product_media_id", UUID.class); } catch (Exception ignored) {}
        if (productImageUrl == null && productMediaId != null) {
            productImageUrl = mediaUrlResolver.resolveUrl(productMediaId).orElse(null);
        }
        List<String> imageUrls = productImageUrl != null ? new ArrayList<>(List.of(productImageUrl)) : new ArrayList<>();

        return ProductResponse.builder()
                .id(id)
                .slug(normalizeText(rs.getString("product_slug"), id.toString()))
                .sku(normalizeText(rs.getString("product_sku"), id.toString()))
                .category(ProductResponse.CategoryInfo.builder()
                        .id(categorySlug != null ? categorySlug : categoryId != null ? categoryId.toString() : null)
                        .label(categoryName)
                        .path(categoryPath)
                        .parentId(parentCategorySlug)
                        .parentLabel(parentCategoryName)
                        .build())
                .categoryName(categoryName)
                .name(normalizeText(rs.getString("product_name"), "Sản phẩm"))
                .description(normalizeText(rs.getString("product_description"), ""))
                .status(normalizeText(rs.getString("product_status"), "ACTIVE"))
                .rating(getNullableDouble(rs, "avg_rating"))
                .ratingCount(rs.getInt("review_count"))
                .soldCount(rs.getInt("sold_count"))
                .supports3d(rs.getBoolean("supports_3d"))
                .features(features)
                .price(0.0)
                .roomTypeHint(categoryName)
                .imageUrls(imageUrls)
                .build();
    }

    private Map<UUID, List<ProductResponse.VariantDto>> fetchVariantsInBatch(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) return Collections.emptyMap();

        String sql = """
                SELECT
                    pv.id,
                    pv.product_id,
                    COALESCE(pv.price, p.base_price) AS price,
                    pv.stock_quantity,
                    COALESCE(pv.weight, p.weight) AS weight,
                    COALESCE(pv.length, p.length) AS length,
                    COALESCE(pv.width, p.width) AS width,
                    COALESCE(pv.height, p.height) AS height,
                    pv.color,
                    pv.material,
                    pv.warranty,
                    pv.sku,
                    pv.low_stock_threshold,
                    pv.supports_3d,
                    pv.model_media_id,
                    pv.model_url,
                    COALESCE(pv.specifications, p.specifications) AS specifications,
                    p.features AS features
                FROM product_variants pv
                JOIN products p ON pv.product_id = p.id
                WHERE CAST(pv.product_id AS text) IN (:productIdsText)
                ORDER BY pv.product_id, price ASC NULLS LAST
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, Map.of("productIdsText", productIds.stream().map(UUID::toString).toList()));

        List<UUID> variantIds = new ArrayList<>();
        Map<UUID, ProductResponse.VariantDto> variantMap = new HashMap<>();
        Map<UUID, List<ProductResponse.VariantDto>> productVariantsMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            UUID id = (UUID) row.get("id");
            UUID productId = (UUID) row.get("product_id");
            variantIds.add(id);

            ProductResponse.VariantDto variant = ProductResponse.VariantDto.builder()
                    .id(id)
                    .price(row.get("price") == null ? null : ((Number) row.get("price")).doubleValue())
                    .stockQuantity(row.get("stock_quantity") == null ? 0 : ((Number) row.get("stock_quantity")).intValue())
                    .weight(row.get("weight") == null ? null : ((Number) row.get("weight")).doubleValue())
                    .length(row.get("length") == null ? null : ((Number) row.get("length")).doubleValue())
                    .width(row.get("width") == null ? null : ((Number) row.get("width")).doubleValue())
                    .height(row.get("height") == null ? null : ((Number) row.get("height")).doubleValue())
                    .color(normalizeText((String) row.get("color"), ""))
                    .material(normalizeText((String) row.get("material"), ""))
                    .warranty(normalizeText((String) row.get("warranty"), ""))
                    .sku(normalizeText((String) row.get("sku"), ""))
                    .lowStockThreshold(row.get("low_stock_threshold") == null ? 0 : ((Number) row.get("low_stock_threshold")).intValue())
                    .supports3d(row.get("supports_3d") != null && (Boolean) row.get("supports_3d"))
                    .modelMediaId((UUID) row.get("model_media_id"))
                    .modelUrl(normalizeText((String) row.get("model_url"), ""))
                    .specifications(parseSpecifications(row.get("specifications")))
                    .features(parseFeatures(row.get("features")))
                    .imageUrls(new ArrayList<>())
                    .build();

            variantMap.put(id, variant);
            productVariantsMap.computeIfAbsent(productId, k -> new ArrayList<>()).add(variant);
        }

        if (!variantIds.isEmpty()) {
            Map<UUID, List<String>> imagesMap = fetchVariantImagesInBatch(variantIds);
            for (Map.Entry<UUID, ProductResponse.VariantDto> entry : variantMap.entrySet()) {
                entry.getValue().setImageUrls(imagesMap.getOrDefault(entry.getKey(), new ArrayList<>()));
            }
        }

        return productVariantsMap;
    }

    private Map<UUID, List<String>> fetchVariantImagesInBatch(List<UUID> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return Collections.emptyMap();

        String sql = """
                SELECT variant_id, image_url, media_id
                FROM product_variant_images
                WHERE CAST(variant_id AS text) IN (:variantIdsText)
                ORDER BY variant_id, position ASC
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, Map.of("variantIdsText", variantIds.stream().map(UUID::toString).toList()));
        Map<UUID, List<String>> result = new HashMap<>();

        for (Map<String, Object> row : rows) {
            UUID variantId = (UUID) row.get("variant_id");
            String imageUrl = (String) row.get("image_url");
            UUID mediaId = (UUID) row.get("media_id");
            if (imageUrl == null && mediaId != null) {
                imageUrl = mediaUrlResolver.resolveUrl(mediaId).orElse(null);
            }
            if (imageUrl != null) {
                result.computeIfAbsent(variantId, k -> new ArrayList<>()).add(imageUrl);
            }
        }

        return result;
    }

    private List<ProductResponse.VariantDto> fetchVariants(UUID productId) {
        String sql = """
                SELECT
                    pv.id,
                    COALESCE(pv.price, p.base_price) AS price,
                    pv.stock_quantity,
                    COALESCE(pv.weight, p.weight) AS weight,
                    COALESCE(pv.length, p.length) AS length,
                    COALESCE(pv.width, p.width) AS width,
                    COALESCE(pv.height, p.height) AS height,
                    pv.color,
                    pv.material,
                    pv.warranty,
                    pv.sku,
                    pv.low_stock_threshold,
                    pv.supports_3d,
                    pv.model_media_id,
                    pv.model_url,
                    COALESCE(pv.specifications, p.specifications) AS specifications,
                    p.features AS features
                FROM product_variants pv
                JOIN products p ON pv.product_id = p.id
                WHERE pv.product_id = :productId
                ORDER BY price ASC NULLS LAST
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("productId", productId),
                (rs, rowNum) -> ProductResponse.VariantDto.builder()
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
                        .sku(normalizeText(rs.getString("sku"), ""))
                        .lowStockThreshold(rs.getInt("low_stock_threshold"))
                        .supports3d(rs.getBoolean("supports_3d"))
                        .modelMediaId((UUID) rs.getObject("model_media_id"))
                        .modelUrl(normalizeText(rs.getString("model_url"), ""))
                        .specifications(parseSpecifications(rs.getObject("specifications")))
                        .features(parseFeatures(rs.getObject("features")))
                        .imageUrls(fetchVariantImages((UUID) rs.getObject("id")))
                        .build());
    }

    private List<String> fetchVariantImages(UUID variantId) {
        String sql = """
                SELECT image_url, media_id
                FROM product_variant_images
                WHERE variant_id = :variantId
                ORDER BY position ASC
                """;

        List<String> results = jdbcTemplate.query(
                sql,
                Map.of("variantId", variantId),
                (rs, rowNum) -> {
                    String img = rs.getString("image_url");
                    UUID mediaId = rs.getObject("media_id", UUID.class);
                    if (img == null && mediaId != null) {
                        img = mediaUrlResolver.resolveUrl(mediaId).orElse(null);
                    }
                    return img;
                });
        return results.stream().filter(Objects::nonNull).toList();
    }

    private Map<UUID, List<String>> fetchGalleryInBatch(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) return Collections.emptyMap();

        String sql = """
                SELECT id AS product_id, image_url, image_media_id AS media_id
                FROM products
                WHERE CAST(id AS text) IN (:productIdsText)
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, Map.of("productIdsText", productIds.stream().map(UUID::toString).toList()));
        Map<UUID, List<String>> result = new HashMap<>();

        for (Map<String, Object> row : rows) {
            UUID productId = (UUID) row.get("product_id");
            String imageUrl = (String) row.get("image_url");
            UUID mediaId = (UUID) row.get("media_id");
            if (imageUrl == null && mediaId != null) {
                imageUrl = mediaUrlResolver.resolveUrl(mediaId).orElse(null);
            }
            if (imageUrl != null) {
                result.computeIfAbsent(productId, k -> new ArrayList<>()).add(imageUrl);
            }
        }

        return result;
    }

    private List<String> fetchGallery(UUID productId) {
        String sql = """
                SELECT image_url, image_media_id AS media_id
                FROM products
                WHERE id = :productId
                """;

        List<String> results = jdbcTemplate.query(
                sql,
                Map.of("productId", productId),
                (rs, rowNum) -> {
                    String img = rs.getString("image_url");
                    UUID mediaId = rs.getObject("media_id", UUID.class);
                    if (img == null && mediaId != null) {
                        img = mediaUrlResolver.resolveUrl(mediaId).orElse(null);
                    }
                    return img;
                });
        return results.stream().filter(Objects::nonNull).toList();
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

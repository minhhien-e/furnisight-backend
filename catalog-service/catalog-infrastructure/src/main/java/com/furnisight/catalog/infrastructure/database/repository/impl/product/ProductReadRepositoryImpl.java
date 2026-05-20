package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.projection.ProductSummaryProjection;
import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductReadRepositoryImpl implements ProductReadRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ProductDetailProjection> findProductDetailBySlug(String slug) {
        String sql = "SELECT " +
                " p.id as product_id, p.category_id, p.shop_id, p.name as product_name, p.slug as product_slug, " +
                " p.description as product_description, p.product_status, p.attributes as product_attributes, " +
                " p.metadata as product_metadata, p.specs as product_specs, p.collection_id, p.view_count, " +
                " p.created_at, p.updated_at, " +
                " c.name as category_name, c.slug as category_slug, " +
                " pc.name as parent_category_name, pc.slug as parent_category_slug, " +
                " col.name as collection_name " +
                " FROM products p " +
                " LEFT JOIN categories c ON p.category_id = c.id " +
                " LEFT JOIN categories pc ON c.parent_id = pc.id " +
                " LEFT JOIN collections col ON p.collection_id = col.id " +
                " WHERE p.slug = :slug";

        return jdbcTemplate.query(sql, Map.of("slug", slug), rs -> {
            if (rs.next()) {
                ProductDetailProjection dto = mapRowToProductDto(rs);
                UUID productId = dto.getId();
                List<ProductDetailProjection.VariantDto> variants = fetchVariants(productId);
                dto.setVariants(variants);
                if (!variants.isEmpty()) {
                    dto.setPrice(variants.get(0).getPrice());

                    // Aggregate distinct materials
                    dto.setMaterials(variants.stream()
                            .map(ProductDetailProjection.VariantDto::getMaterial)
                            .filter(m -> m != null && !m.isBlank())
                            .distinct()
                            .collect(Collectors.toList()));

                    // Aggregate distinct colors
                    dto.setColors(variants.stream()
                            .map(ProductDetailProjection.VariantDto::getColor)
                            .filter(c -> c != null && !c.isBlank())
                            .distinct()
                            .collect(Collectors.toList()));

                    // Aggregate distinct sizes
                    dto.setSizes(variants.stream()
                            .map(ProductDetailProjection.VariantDto::getSize)
                            .filter(s -> s != null && !s.isBlank())
                            .distinct()
                            .collect(Collectors.toList()));

                    // Aggregate total stock
                    dto.setStock(variants.stream()
                            .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                            .sum());
                }
                List<String> galleryList = fetchGallery(productId);
                if (galleryList.isEmpty() && dto.getThumbnailUrl() != null) {
                    galleryList = List.of(dto.getThumbnailUrl(), dto.getThumbnailUrl());
                }
                dto.setGallery(galleryList);
                
                return Optional.of(dto);
            }
            return Optional.empty();
        });
    }

    @Override
    public Optional<ProductDetailProjection> findProductDetailById(UUID productId) {
        String sql = "SELECT " +
                " p.id as product_id, p.category_id, p.shop_id, p.name as product_name, p.slug as product_slug, " +
                " p.description as product_description, p.product_status, p.attributes as product_attributes, " +
                " p.metadata as product_metadata, p.specs as product_specs, p.collection_id, p.view_count, " +
                " p.created_at, p.updated_at, " +
                " c.name as category_name, c.slug as category_slug, " +
                " pc.name as parent_category_name, pc.slug as parent_category_slug, " +
                " col.name as collection_name " +
                " FROM products p " +
                " LEFT JOIN categories c ON p.category_id = c.id " +
                " LEFT JOIN categories pc ON c.parent_id = pc.id " +
                " LEFT JOIN collections col ON p.collection_id = col.id " +
                " WHERE p.id = :productId";

        return jdbcTemplate.query(sql, Map.of("productId", productId), rs -> {
            if (rs.next()) {
                ProductDetailProjection dto = mapRowToProductDto(rs);
                List<ProductDetailProjection.VariantDto> variants = fetchVariants(productId);
                dto.setVariants(variants);
                if (!variants.isEmpty()) {
                    dto.setPrice(variants.get(0).getPrice());

                    // Aggregate distinct materials
                    dto.setMaterials(variants.stream()
                            .map(ProductDetailProjection.VariantDto::getMaterial)
                            .filter(m -> m != null && !m.isBlank())
                            .distinct()
                            .collect(Collectors.toList()));

                    // Aggregate distinct colors
                    dto.setColors(variants.stream()
                            .map(ProductDetailProjection.VariantDto::getColor)
                            .filter(c -> c != null && !c.isBlank())
                            .distinct()
                            .collect(Collectors.toList()));

                    // Aggregate distinct sizes
                    dto.setSizes(variants.stream()
                            .map(ProductDetailProjection.VariantDto::getSize)
                            .filter(s -> s != null && !s.isBlank())
                            .distinct()
                            .collect(Collectors.toList()));

                    // Aggregate total stock
                    dto.setStock(variants.stream()
                            .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                            .sum());
                }
                List<String> galleryList = fetchGallery(productId);
                if (galleryList.isEmpty() && dto.getThumbnailUrl() != null) {
                    galleryList = List.of(dto.getThumbnailUrl(), dto.getThumbnailUrl());
                }
                dto.setGallery(galleryList);

                return Optional.of(dto);
            }
            return Optional.empty();
        });
    }

    @Override
    public SearchProductsProjection searchProducts(SearchProductsQuery queryParam) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        // 1. Full-text search
        if (queryParam.getQ() != null && !queryParam.getQ().isBlank()) {
            whereClause.append(" AND (LOWER(p.name) LIKE LOWER(:q) OR LOWER(p.description) LIKE LOWER(:q))");
            params.put("q", "%" + queryParam.getQ().trim() + "%");
        }

        // 2. Status filter
        String status = queryParam.getStatus() != null ? queryParam.getStatus().toUpperCase() : "ACTIVE";
        whereClause.append(" AND p.product_status = :status");
        params.put("status", status);

        // 3. Category filter
        if (queryParam.getCategory() != null && !queryParam.getCategory().isBlank()) {
            UUID categoryId = null;
            try {
                categoryId = UUID.fromString(queryParam.getCategory());
            } catch (IllegalArgumentException e) {
                String catSql = "SELECT id FROM categories WHERE slug = :slug LIMIT 1";
                List<UUID> catIds = jdbcTemplate.query(catSql, Map.of("slug", queryParam.getCategory()),
                        (rs, rowNum) -> (UUID) rs.getObject("id"));
                if (!catIds.isEmpty()) {
                    categoryId = catIds.get(0);
                }
            }
            if (categoryId != null) {
                whereClause.append(" AND p.category_id = :categoryId");
                params.put("categoryId", categoryId);
            } else {
                whereClause.append(" AND 1=0");
            }
        }

        // 4. Colors filter
        if (queryParam.getColors() != null && !queryParam.getColors().isEmpty()) {
            whereClause.append(
                    " AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.attributes->>'color' IN (:colors))");
            params.put("colors", queryParam.getColors());
        }

        // 5. Materials filter
        if (queryParam.getMaterials() != null && !queryParam.getMaterials().isEmpty()) {
            whereClause.append(
                    " AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.attributes->>'material' IN (:materials))");
            params.put("materials", queryParam.getMaterials());
        }

        // 6. Price filter (Bands)
        if (queryParam.getPriceBands() != null && !queryParam.getPriceBands().isEmpty()) {
            StringBuilder bandSql = new StringBuilder(
                    " AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND (");
            boolean first = true;
            for (String band : queryParam.getPriceBands()) {
                if (!first) {
                    bandSql.append(" OR ");
                }
                first = false;
                switch (band) {
                    case "lt5m" -> bandSql.append("pv.price < 5000000.0");
                    case "5-15m" -> bandSql.append("(pv.price >= 5000000.0 AND pv.price <= 15000000.0)");
                    case "15-30m" -> bandSql.append("(pv.price >= 15000000.0 AND pv.price <= 30000000.0)");
                    case "gt30m" -> bandSql.append("pv.price > 30000000.0");
                    default -> bandSql.append("1=1");
                }
            }
            bandSql.append("))");
            whereClause.append(bandSql);
        }

        // 7. Price slider filter (maxPrice <= maxPriceSlider)
        if (queryParam.getPriceSliderPct() != null && !queryParam.getPriceSliderPct().isEmpty()) {
            Double pct = queryParam.getPriceSliderPct().get(0);
            if (pct < 100) {
                double maxPrice = (pct / 100.0) * 50000000.0;
                whereClause.append(
                        " AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id AND pv.price <= :maxPrice)");
                params.put("maxPrice", maxPrice);
            }
        }

        String countSql = "SELECT COUNT(*) FROM products p" + whereClause.toString();
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        String orderBy = " ORDER BY p.created_at DESC";
        if (queryParam.getSort() != null) {
            switch (queryParam.getSort().toLowerCase()) {
                case "popular" -> orderBy = " ORDER BY p.view_count DESC";
                case "rating" -> orderBy = " ORDER BY p.created_at DESC";
                case "newest" -> orderBy = " ORDER BY p.created_at DESC";
            }
        }

        int page = queryParam.getPage() > 0 ? queryParam.getPage() : 0;
        int size = queryParam.getSize() > 0 ? queryParam.getSize() : 24;
        int offset = page * size;

        params.put("limit", size);
        params.put("offset", offset);

        String mainSql = "SELECT " +
                " p.id as product_id, p.category_id, p.shop_id, p.name as product_name, p.slug as product_slug, " +
                " p.description as product_description, p.product_status, p.attributes as product_attributes, " +
                " p.metadata as product_metadata, p.specs as product_specs, p.collection_id, p.view_count, " +
                " p.created_at, p.updated_at, " +
                " c.name as category_name, c.slug as category_slug, " +
                " pc.name as parent_category_name, pc.slug as parent_category_slug, " +
                " col.name as collection_name, " +
                " (SELECT MIN(pv.price) FROM product_variants pv WHERE pv.product_id = p.id) as product_price " +
                " FROM products p " +
                " LEFT JOIN categories c ON p.category_id = c.id " +
                " LEFT JOIN categories pc ON c.parent_id = pc.id " +
                " LEFT JOIN collections col ON p.collection_id = col.id " +
                whereClause.toString() +
                orderBy +
                " LIMIT :limit OFFSET :offset";

        List<ProductSummaryProjection> products = jdbcTemplate.query(mainSql, params,
                (rs, rowNum) -> mapRowToProductSummary(rs));

        // Build in-memory facets based on the returned page results (extremely fast and
        // clean)
        Map<String, Long> catCounts = products.stream()
                .filter(p -> p.getCategoryName() != null)
                .collect(Collectors.groupingBy(ProductSummaryProjection::getCategoryName, Collectors.counting()));

        List<SearchProductsProjection.CategoryFacet> categoryFacets = catCounts.entrySet().stream()
                .map(entry -> SearchProductsProjection.CategoryFacet.builder()
                        .id(entry.getKey())
                        .slug(entry.getKey().toLowerCase().replace(" ", "-"))
                        .label(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        String materialsSql = "SELECT DISTINCT pv.attributes->>'material' as mat FROM product_variants pv WHERE pv.attributes->>'material' IS NOT NULL";
        List<String> distinctMaterials = jdbcTemplate.query(materialsSql, Map.of(),
                (rs, rowNum) -> rs.getString("mat"));
        List<SearchProductsProjection.MaterialFacet> materialFacets = distinctMaterials.stream()
                .map(mat -> SearchProductsProjection.MaterialFacet.builder()
                        .id(mat)
                        .label(mat.substring(0, 1).toUpperCase() + mat.substring(1))
                        .build())
                .collect(Collectors.toList());

        String colorsSql = "SELECT DISTINCT pv.attributes->>'color' as col FROM product_variants pv WHERE pv.attributes->>'color' IS NOT NULL";
        List<String> distinctColors = jdbcTemplate.query(colorsSql, Map.of(), (rs, rowNum) -> rs.getString("col"));
        List<SearchProductsProjection.ColorFacet> colorFacets = distinctColors.stream()
                .map(col -> SearchProductsProjection.ColorFacet.builder()
                        .id(col)
                        .label(col.substring(0, 1).toUpperCase() + col.substring(1))
                        .hex(getColorHex(col))
                        .build())
                .collect(Collectors.toList());

        SearchProductsProjection.Facets facets = SearchProductsProjection.Facets.builder()
                .categories(categoryFacets)
                .materials(materialFacets)
                .colors(colorFacets)
                .build();

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
        String mainSql = "SELECT " +
                " p.id as product_id, p.category_id, p.shop_id, p.name as product_name, p.slug as product_slug, " +
                " p.description as product_description, p.product_status, p.attributes as product_attributes, " +
                " p.metadata as product_metadata, p.specs as product_specs, p.collection_id, p.view_count, " +
                " p.created_at, p.updated_at, " +
                " c.name as category_name, c.slug as category_slug, " +
                " pc.name as parent_category_name, pc.slug as parent_category_slug, " +
                " col.name as collection_name, " +
                " (SELECT MIN(pv.price) FROM product_variants pv WHERE pv.product_id = p.id) as product_price " +
                " FROM products p " +
                " LEFT JOIN categories c ON p.category_id = c.id " +
                " LEFT JOIN categories pc ON c.parent_id = pc.id " +
                " LEFT JOIN collections col ON p.collection_id = col.id " +
                " WHERE p.product_status = 'ACTIVE' " +
                " ORDER BY p.view_count DESC " +
                " LIMIT :limit";

        return jdbcTemplate.query(mainSql, Map.of("limit", limit), (rs, rowNum) -> mapRowToProductSummary(rs));
    }

    private ProductSummaryProjection mapRowToProductSummary(java.sql.ResultSet rs) throws java.sql.SQLException {
        UUID id = (UUID) rs.getObject("product_id");
        String productName = rs.getString("product_name");
        String productSlug = rs.getString("product_slug");
        String categoryName = rs.getString("category_name");
        if (categoryName == null) {
            categoryName = "Sản phẩm";
        }
        Double price = rs.getDouble("product_price");
        if (rs.wasNull()) {
            price = 0.0;
        }

        Map<String, Object> attributes = parseJsonMapObject(rs.getString("product_attributes"));
        String imageUrl = null;
        if (attributes != null && attributes.get("image") != null) {
            imageUrl = attributes.get("image").toString();
        }

        Map<String, Object> metadata = parseJsonMapObject(rs.getString("product_metadata"));
        List<String> tagsList = List.of("new");
        if (metadata != null && metadata.get("tags") instanceof List<?> list) {
            tagsList = list.stream().map(Object::toString).toList();
        }

        return ProductSummaryProjection.builder()
                .id(id)
                .slug(productSlug != null ? productSlug : id.toString())
                .name(productName)
                .categoryName(categoryName)
                .price(price)
                .oldPrice(price > 0 ? price * 1.2 : null)
                .image(imageUrl)
                .rating(4.8)
                .ratingCount(120)
                .tags(tagsList)
                .build();
    }

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
            List<String> galleryList = fetchGallery(product.getId());
            if (galleryList.isEmpty() && product.getThumbnailUrl() != null) {
                galleryList = List.of(product.getThumbnailUrl(), product.getThumbnailUrl());
            }
            product.setGallery(galleryList);
        }
    }

    private ProductDetailProjection mapRowToProductDto(java.sql.ResultSet rs) throws java.sql.SQLException {
        UUID id = (UUID) rs.getObject("product_id");
        UUID categoryId = (UUID) rs.getObject("category_id");
        UUID shopId = (UUID) rs.getObject("shop_id");
        UUID collectionId = (UUID) rs.getObject("collection_id");

        String productName = rs.getString("product_name");
        String productSlug = rs.getString("product_slug");
        String productDescription = rs.getString("product_description");
        String productStatus = rs.getString("product_status");

        Map<String, Object> attributes = parseJsonMapObject(rs.getString("product_attributes"));
        Map<String, Object> metadata = parseJsonMapObject(rs.getString("product_metadata"));

        Map<String, String> attributesMap = new HashMap<>();
        if (attributes != null) {
            attributes.forEach((k, v) -> attributesMap.put(k, v != null ? v.toString() : null));
        }

        String imageUrl = attributesMap.get("image");

        String categoryName = rs.getString("category_name");
        if (categoryName == null) {
            categoryName = "Sản phẩm";
        }
        String categorySlug = rs.getString("category_slug");
        String parentCategoryName = rs.getString("parent_category_name");
        String parentCategorySlug = rs.getString("parent_category_slug");

        String collectionStr = rs.getString("collection_name");
        if (collectionStr == null && metadata != null && metadata.get("collection") != null) {
            collectionStr = metadata.get("collection").toString();
        }

        List<String> tagsList = List.of("new", "sale");
        List<String> featuresList = List.of("Thiết kế hiện đại", "Chất liệu cao cấp", "Bảo hành 12 tháng");
        String modelUrlStr = "/models/sofa.glb";

        if (metadata != null) {
            if (metadata.get("tags") instanceof List<?> list) {
                tagsList = list.stream().map(Object::toString).toList();
            }
            if (metadata.get("features") instanceof List<?> list) {
                featuresList = list.stream().map(Object::toString).toList();
            }
            if (metadata.get("model_url") != null) {
                modelUrlStr = metadata.get("model_url").toString();
            }
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
                .id(categorySlug != null ? categorySlug : (categoryId != null ? categoryId.toString() : "all"))
                .label(categoryName)
                .build());

        return ProductDetailProjection.builder()
                .id(id)
                .slug(id.toString())
                .shopId(shopId)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .category(ProductDetailProjection.CategoryInfo.builder()
                        .id(categorySlug != null ? categorySlug : (categoryId != null ? categoryId.toString() : null))
                        .label(categoryName)
                        .build())
                .name(productName)
                .description(productDescription)
                .thumbnailUrl(imageUrl)
                .image(imageUrl)
                .rating(4.8)
                .ratingCount(120)
                .stock(0)
                .tags(tagsList)
                .materials(new ArrayList<>())
                .colors(new ArrayList<>())
                .sizes(new ArrayList<>())
                .supports3d(true)
                .collection(collectionStr)
                .breadcrumb(breadcrumbList)
                .features(featuresList)
                .status(productStatus)
                .price(0.0)
                .modelUrl(modelUrlStr)
                .roomTypeHint(categoryName)
                .build();
    }

    private List<ProductDetailProjection.VariantDto> fetchVariants(UUID productId) {
        String sql = "SELECT * FROM product_variants WHERE product_id = :productId ORDER BY price ASC";
        return jdbcTemplate.query(sql, Map.of("productId", productId), (rs, rowNum) -> {
            Map<String, String> variantAttrs = parseJsonMap(rs.getString("attributes"));
            return ProductDetailProjection.VariantDto.builder()
                    .id(UUID.fromString(rs.getString("id")))
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

    private List<String> fetchGallery(UUID productId) {
        String sql = "SELECT image_url FROM product_images WHERE product_id = :productId ORDER BY sort_order ASC";
        return jdbcTemplate.query(sql, Map.of("productId", productId), (rs, rowNum) -> rs.getString("image_url"));
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

    private Map<String, String> parseJsonMap(String json) {
        if (json == null || json.isBlank())
            return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse JSON map from value: {}", json, e);
            return new HashMap<>();
        }
    }

    private Map<String, Object> parseJsonMapObject(String json) {
        if (json == null || json.isBlank())
            return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse JSON map object from value: {}", json, e);
            return new HashMap<>();
        }
    }
}

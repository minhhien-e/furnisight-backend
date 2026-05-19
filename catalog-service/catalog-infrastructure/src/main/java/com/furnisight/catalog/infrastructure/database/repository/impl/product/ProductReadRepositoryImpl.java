package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import com.furnisight.catalog.application.product.dto.projection.ProductEsProjection;
import com.furnisight.catalog.infrastructure.elasticsearch.mapper.ProductEsMapper;
import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;
import com.furnisight.catalog.infrastructure.elasticsearch.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import com.furnisight.catalog.infrastructure.database.repository.jpa.product.ProductJpaRepository;
import com.furnisight.catalog.infrastructure.database.repository.jpa.category.CategoryJpaRepository;
import com.furnisight.catalog.infrastructure.database.repository.jpa.collection.CollectionJpaRepository;

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
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductJpaRepository productJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final CollectionJpaRepository collectionJpaRepository;

    @Override
    public Optional<ProductDetailProjection> findProductDetailById(UUID productId) {
        return productJpaRepository.findById(productId).map(product -> {
            ProductDetailProjection dto = mapProductToDto(product);
            List<ProductDetailProjection.VariantDto> variants = fetchVariants(productId);
            dto.setVariants(variants);
            if (!variants.isEmpty()) {
                dto.setPrice(variants.get(0).getPrice());
            }
            return dto;
        });
    }

    @Override
    public SearchProductsProjection searchProducts(SearchProductsQuery queryParam) {
        try {
            log.debug("Executing searchProducts with query: {} in Elasticsearch", queryParam.getQ());
            return searchProductsFromEs(queryParam);
        } catch (Exception e) {
            log.error("Elasticsearch search failed or index not found. Falling back to PostgreSQL database.", e);
            return searchProductsFromDb(queryParam);
        }
    }

    public SearchProductsProjection searchProductsFromEs(SearchProductsQuery queryParam) {
        Criteria criteria = new Criteria();

        // 1. Full-text search
        if (queryParam.getQ() != null && !queryParam.getQ().isBlank()) {
            criteria = criteria.and(new Criteria("name").contains(queryParam.getQ())
                    .or(new Criteria("description").contains(queryParam.getQ())));
        }

        // 2. Status filter
        String status = queryParam.getStatus() != null ? queryParam.getStatus().toUpperCase() : "ACTIVE";
        criteria = criteria.and(new Criteria("status").is(status));

        // 3. Category filter
        if (queryParam.getCategory() != null && !queryParam.getCategory().isBlank()) {
            criteria = criteria.and(new Criteria("categorySlug").is(queryParam.getCategory())
                    .or(new Criteria("categoryId").is(queryParam.getCategory())));
        }

        // 4. Colors filter
        if (queryParam.getColors() != null && !queryParam.getColors().isEmpty()) {
            criteria = criteria.and(new Criteria("colors").in(queryParam.getColors()));
        }

        // 5. Materials filter
        if (queryParam.getMaterials() != null && !queryParam.getMaterials().isEmpty()) {
            criteria = criteria.and(new Criteria("materials").in(queryParam.getMaterials()));
        }

        // 6. Price filter (Bands)
        if (queryParam.getPriceBands() != null && !queryParam.getPriceBands().isEmpty()) {
            Criteria priceBandCriteria = new Criteria();
            boolean first = true;
            for (String band : queryParam.getPriceBands()) {
                Criteria sub = new Criteria("minPrice");
                switch (band) {
                    case "lt5m" -> sub = sub.lessThan(5000000.0);
                    case "5-15m" -> sub = sub.greaterThanEqual(5000000.0).lessThanEqual(15000000.0);
                    case "15-30m" -> sub = sub.greaterThanEqual(15000000.0).lessThanEqual(30000000.0);
                    case "gt30m" -> sub = sub.greaterThan(30000000.0);
                }
                if (first) {
                    priceBandCriteria = sub;
                    first = false;
                } else {
                    priceBandCriteria = priceBandCriteria.or(sub);
                }
            }
            criteria = criteria.and(priceBandCriteria);
        }

        // 7. Price slider filter (maxPrice <= maxPriceSlider)
        if (queryParam.getPriceSliderPct() != null && !queryParam.getPriceSliderPct().isEmpty()) {
            Double pct = queryParam.getPriceSliderPct().get(0);
            if (pct < 100) {
                double maxPrice = (pct / 100.0) * 50000000.0;
                criteria = criteria.and(new Criteria("minPrice").lessThanEqual(maxPrice));
            }
        }

        // 8. Sorting
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (queryParam.getSort() != null) {
            switch (queryParam.getSort().toLowerCase()) {
                case "popular" -> sort = Sort.by(Sort.Direction.DESC, "viewCount");
                case "rating" -> sort = Sort.by(Sort.Direction.DESC, "rating");
                case "newest" -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
            }
        }

        // 9. Pagination
        int page = queryParam.getPage() > 0 ? queryParam.getPage() : 0;
        int size = queryParam.getSize() > 0 ? queryParam.getSize() : 24;
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
        criteriaQuery.setPageable(pageRequest);

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(criteriaQuery, ProductDocument.class);

        List<ProductDetailProjection> products = searchHits.getSearchHits().stream()
                .map(hit -> ProductEsMapper.toDetailProjection(hit.getContent()))
                .collect(Collectors.toList());

        long total = searchHits.getTotalHits();

        // Build in-memory facets based on returned results (high performance)
        Map<String, Long> catCounts = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(doc -> doc.getCategoryId() != null)
                .collect(Collectors.groupingBy(ProductDocument::getCategoryId, Collectors.counting()));

        List<SearchProductsProjection.CategoryFacet> categoryFacets = catCounts.entrySet().stream()
                .map(entry -> {
                    ProductDocument doc = searchHits.getSearchHits().stream()
                            .map(SearchHit::getContent)
                            .filter(d -> entry.getKey().equals(d.getCategoryId()))
                            .findFirst().orElse(null);
                    return SearchProductsProjection.CategoryFacet.builder()
                            .id(entry.getKey())
                            .slug(doc != null ? doc.getCategorySlug() : entry.getKey())
                            .label(doc != null ? doc.getCategoryName() : "Sản phẩm")
                            .count(entry.getValue())
                            .build();
                }).collect(Collectors.toList());

        List<SearchProductsProjection.MaterialFacet> materialFacets = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .flatMap(doc -> doc.getMaterials() != null ? doc.getMaterials().stream()
                        : java.util.stream.Stream.empty())
                .filter(Objects::nonNull).distinct()
                .map(mat -> SearchProductsProjection.MaterialFacet.builder()
                        .id(mat)
                        .label(mat.substring(0, 1).toUpperCase() + mat.substring(1))
                        .build())
                .collect(Collectors.toList());

        List<SearchProductsProjection.ColorFacet> colorFacets = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .flatMap(doc -> doc.getColors() != null ? doc.getColors().stream() : java.util.stream.Stream.empty())
                .filter(Objects::nonNull).distinct()
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

    public SearchProductsProjection searchProductsFromDb(SearchProductsQuery queryParam) {
        log.info("Executing searchProducts fallback via JPA and mapping to Projection. Query: {}", queryParam.getQ());
        
        // 1. Resolve Category ID by slug or ID string if provided
        UUID categoryId = null;
        if (queryParam.getCategory() != null && !queryParam.getCategory().isBlank()) {
            try {
                categoryId = UUID.fromString(queryParam.getCategory());
            } catch (IllegalArgumentException e) {
                categoryId = categoryJpaRepository.findBySlug(queryParam.getCategory())
                        .map(com.furnisight.catalog.domain.entities.category.Category::getId)
                        .orElse(null);
            }
        }

        // 2. Resolve ProductStatus
        com.furnisight.catalog.domain.enums.product.ProductStatus status = com.furnisight.catalog.domain.enums.product.ProductStatus.ACTIVE;
        if (queryParam.getStatus() != null && !queryParam.getStatus().isBlank()) {
            try {
                status = com.furnisight.catalog.domain.enums.product.ProductStatus.valueOf(queryParam.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Fallback to ACTIVE
            }
        }

        // 3. Setup Pagination and Sorting
        int page = queryParam.getPage() > 0 ? queryParam.getPage() : 0;
        int size = queryParam.getSize() > 0 ? queryParam.getSize() : 24;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (queryParam.getSort() != null) {
            switch (queryParam.getSort().toLowerCase()) {
                case "popular" -> sort = Sort.by(Sort.Direction.DESC, "viewCount");
                case "rating" -> sort = Sort.by(Sort.Direction.DESC, "rating");
                case "newest" -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
            }
        }
        Pageable pageable = PageRequest.of(page, size, sort);

        // 4. Query from database using JPA
        org.springframework.data.domain.Page<com.furnisight.catalog.domain.entities.product.Product> productPage = 
                productJpaRepository.searchProducts(queryParam.getQ(), categoryId, status, pageable);

        // 5. Map JPA Products to ProductDetailProjections
        List<ProductDetailProjection> products = productPage.getContent().stream()
                .map(this::mapProductToDto)
                .collect(Collectors.toList());

        // 6. Populate variants and aggregates (materials, colors, sizes, stock, price, etc.)
        populateProductVariantsAndAggregates(products);

        // 7. Build in-memory facets based on the returned page results (extremely fast and clean)
        Map<String, Long> catCounts = products.stream()
                .filter(p -> p.getCategoryId() != null)
                .collect(Collectors.groupingBy(p -> p.getCategoryId().toString(), Collectors.counting()));

        List<SearchProductsProjection.CategoryFacet> categoryFacets = catCounts.entrySet().stream()
                .map(entry -> {
                    ProductDetailProjection p = products.stream()
                            .filter(prod -> prod.getCategoryId() != null && entry.getKey().equals(prod.getCategoryId().toString()))
                            .findFirst().orElse(null);
                    return SearchProductsProjection.CategoryFacet.builder()
                            .id(entry.getKey())
                            .slug(p != null && p.getCategory() != null ? p.getCategory().getId() : entry.getKey())
                            .label(p != null ? p.getCategoryName() : "Sản phẩm")
                            .count(entry.getValue())
                            .build();
                }).collect(Collectors.toList());

        List<SearchProductsProjection.MaterialFacet> materialFacets = products.stream()
                .flatMap(p -> p.getMaterials() != null ? p.getMaterials().stream() : java.util.stream.Stream.empty())
                .filter(Objects::nonNull).distinct()
                .map(mat -> SearchProductsProjection.MaterialFacet.builder()
                        .id(mat)
                        .label(mat.substring(0, 1).toUpperCase() + mat.substring(1))
                        .build())
                .collect(Collectors.toList());

        List<SearchProductsProjection.ColorFacet> colorFacets = products.stream()
                .flatMap(p -> p.getColors() != null ? p.getColors().stream() : java.util.stream.Stream.empty())
                .filter(Objects::nonNull).distinct()
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
                .total(productPage.getTotalElements())
                .page(page + 1)
                .pageSize(size)
                .facets(facets)
                .build();
    }

    private ProductDetailProjection mapProductToDto(com.furnisight.catalog.domain.entities.product.Product product) {
        Map<String, String> attributesMap = new HashMap<>();
        if (product.getAttributes() != null) {
            product.getAttributes().forEach((k, v) -> attributesMap.put(k, v != null ? v.toString() : null));
        }

        String imageUrl = attributesMap.get("image");

        String categoryName = "Sản phẩm";
        String categorySlug = null;
        String parentCategoryName = null;
        String parentCategorySlug = null;

        if (product.getCategoryId() != null) {
            Optional<com.furnisight.catalog.domain.entities.category.Category> catOpt = categoryJpaRepository.findById(product.getCategoryId());
            if (catOpt.isPresent()) {
                com.furnisight.catalog.domain.entities.category.Category category = catOpt.get();
                categoryName = category.getName() != null ? category.getName().getValue() : "Sản phẩm";
                categorySlug = category.getSlug() != null ? category.getSlug().getValue() : null;
                if (category.getParentId() != null) {
                    Optional<com.furnisight.catalog.domain.entities.category.Category> parentCatOpt = categoryJpaRepository.findById(category.getParentId());
                    if (parentCatOpt.isPresent()) {
                        com.furnisight.catalog.domain.entities.category.Category parentCategory = parentCatOpt.get();
                        parentCategoryName = parentCategory.getName() != null ? parentCategory.getName().getValue() : null;
                        parentCategorySlug = parentCategory.getSlug() != null ? parentCategory.getSlug().getValue() : null;
                    }
                }
            }
        }

        String collectionStr = null;
        if (product.getCollectionId() != null) {
            collectionStr = collectionJpaRepository.findById(product.getCollectionId())
                    .map(com.furnisight.catalog.domain.entities.collection.Collection::getName)
                    .orElse(null);
        }

        List<String> tagsList = List.of("new", "sale");
        List<String> featuresList = List.of("Thiết kế hiện đại", "Chất liệu cao cấp", "Bảo hành 12 tháng");
        String modelUrlStr = "/models/sofa.glb";

        if (product.getMetadata() != null) {
            if (product.getMetadata().get("tags") instanceof List<?> list) {
                tagsList = list.stream().map(Object::toString).toList();
            }
            if (product.getMetadata().get("features") instanceof List<?> list) {
                featuresList = list.stream().map(Object::toString).toList();
            }
            if (product.getMetadata().get("model_url") != null) {
                modelUrlStr = product.getMetadata().get("model_url").toString();
            }
            if (collectionStr == null && product.getMetadata().get("collection") != null) {
                collectionStr = product.getMetadata().get("collection").toString();
            }
        }

        List<String> galleryList = new ArrayList<>();
        if (product.getGallery() != null) {
            galleryList = product.getGallery().stream()
                    .map(com.furnisight.catalog.domain.entities.product.ProductImage::getImageUrl)
                    .collect(Collectors.toList());
        }
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
                .id(categorySlug != null ? categorySlug : (product.getCategoryId() != null ? product.getCategoryId().toString() : "all"))
                .label(categoryName)
                .build());

        return ProductDetailProjection.builder()
                .id(product.getId())
                .slug(product.getId().toString())
                .shopId(product.getShopId())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .category(ProductDetailProjection.CategoryInfo.builder()
                        .id(categorySlug != null ? categorySlug : (product.getCategoryId() != null ? product.getCategoryId().toString() : null))
                        .label(categoryName)
                        .build())
                .name(product.getName() != null ? product.getName().getValue() : null)
                .description(product.getDescription() != null ? product.getDescription().getValue() : null)
                .thumbnailUrl(imageUrl)
                .image(imageUrl)
                .gallery(galleryList)
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
                .status(product.getProductStatus() != null ? product.getProductStatus().name() : null)
                .price(0.0)
                .modelUrl(modelUrlStr)
                .roomTypeHint(categoryName)
                .build();
    }

    @Override
    public List<ProductDetailProjection> findTopProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount"));
        List<com.furnisight.catalog.domain.entities.product.Product> content = productJpaRepository.searchProducts(
                null, null, com.furnisight.catalog.domain.enums.product.ProductStatus.ACTIVE, pageable).getContent();

        List<ProductDetailProjection> products = content.stream()
                .map(this::mapProductToDto)
                .collect(Collectors.toList());

        populateProductVariantsAndAggregates(products);
        return products;
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



    @Override
    public Optional<ProductEsProjection> findProductDocumentById(UUID productId) {
        return productJpaRepository.findById(productId).map(product -> {
            List<ProductDetailProjection.VariantDto> variants = fetchVariants(productId);

            double minPrice = variants.stream().mapToDouble(ProductDetailProjection.VariantDto::getPrice).min()
                    .orElse(0.0);
            double maxPrice = variants.stream().mapToDouble(ProductDetailProjection.VariantDto::getPrice).max()
                    .orElse(0.0);
            int totalStock = variants.stream()
                    .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0).sum();

            List<String> colors = variants.stream().map(ProductDetailProjection.VariantDto::getColor)
                    .filter(c -> c != null && !c.isBlank()).distinct().collect(Collectors.toList());
            List<String> materials = variants.stream().map(ProductDetailProjection.VariantDto::getMaterial)
                    .filter(m -> m != null && !m.isBlank()).distinct().collect(Collectors.toList());
            List<String> sizes = variants.stream().map(ProductDetailProjection.VariantDto::getSize)
                    .filter(s -> s != null && !s.isBlank()).distinct().collect(Collectors.toList());

            Map<String, String> attributesMap = new HashMap<>();
            if (product.getAttributes() != null) {
                product.getAttributes().forEach((k, v) -> attributesMap.put(k, v != null ? v.toString() : null));
            }

            String categoryName = "Sản phẩm";
            String categorySlug = null;
            if (product.getCategoryId() != null) {
                Optional<com.furnisight.catalog.domain.entities.category.Category> catOpt = categoryJpaRepository.findById(product.getCategoryId());
                if (catOpt.isPresent()) {
                    categoryName = catOpt.get().getName() != null ? catOpt.get().getName().getValue() : "Sản phẩm";
                    categorySlug = catOpt.get().getSlug() != null ? catOpt.get().getSlug().getValue() : null;
                }
            }

            return ProductEsProjection.builder()
                    .id(productId.toString())
                    .name(product.getName() != null ? product.getName().getValue() : null)
                    .description(product.getDescription() != null ? product.getDescription().getValue() : null)
                    .status(product.getProductStatus() != null ? product.getProductStatus().name() : null)
                    .categoryId(product.getCategoryId() != null ? product.getCategoryId().toString() : null)
                    .categoryName(categoryName)
                    .categorySlug(categorySlug)
                    .shopId(product.getShopId() != null ? product.getShopId().toString() : null)
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .colors(colors)
                    .materials(materials)
                    .sizes(sizes)
                    .totalStock(totalStock)
                    .viewCount(product.getViewCount() != null ? product.getViewCount() : 0)
                    .rating(4.8)
                    .thumbnailUrl(attributesMap.get("image"))
                    .attributes(product.getAttributes())
                    .createdAt(product.getCreatedAt() != null ? product.getCreatedAt() : java.time.LocalDateTime.now())
                    .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt() : java.time.LocalDateTime.now())
                    .build();
        });
    }
}

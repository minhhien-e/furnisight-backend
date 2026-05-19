package com.furnisight.catalog.infrastructure.elasticsearch.mapper;

import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.dto.projection.ProductEsProjection;
import com.furnisight.catalog.infrastructure.elasticsearch.document.ProductDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper thực hiện chuyển đổi qua lại giữa ProductDocument (Infrastructure)
 * và các Projections của tầng Application.
 */
public final class ProductEsMapper {

    private ProductEsMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Chuyển đổi từ ProductDocument (ES Document) sang ProductEsProjection (Application DTO)
     */
    public static ProductEsProjection toProjection(ProductDocument doc) {
        if (doc == null) {
            return null;
        }
        return ProductEsProjection.builder()
                .id(doc.getId())
                .name(doc.getName())
                .description(doc.getDescription())
                .status(doc.getStatus())
                .categoryId(doc.getCategoryId())
                .categoryName(doc.getCategoryName())
                .categorySlug(doc.getCategorySlug())
                .shopId(doc.getShopId())
                .minPrice(doc.getMinPrice())
                .maxPrice(doc.getMaxPrice())
                .colors(doc.getColors())
                .materials(doc.getMaterials())
                .sizes(doc.getSizes())
                .totalStock(doc.getTotalStock())
                .viewCount(doc.getViewCount())
                .rating(doc.getRating())
                .thumbnailUrl(doc.getThumbnailUrl())
                .attributes(doc.getAttributes())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    /**
     * Chuyển đổi từ ProductEsProjection (Application DTO) sang ProductDocument (ES Document)
     */
    public static ProductDocument toDocument(ProductEsProjection projection) {
        if (projection == null) {
            return null;
        }
        return ProductDocument.builder()
                .id(projection.getId())
                .name(projection.getName())
                .description(projection.getDescription())
                .status(projection.getStatus())
                .categoryId(projection.getCategoryId())
                .categoryName(projection.getCategoryName())
                .categorySlug(projection.getCategorySlug())
                .shopId(projection.getShopId())
                .minPrice(projection.getMinPrice())
                .maxPrice(projection.getMaxPrice())
                .colors(projection.getColors())
                .materials(projection.getMaterials())
                .sizes(projection.getSizes())
                .totalStock(projection.getTotalStock())
                .viewCount(projection.getViewCount())
                .rating(projection.getRating())
                .thumbnailUrl(projection.getThumbnailUrl())
                .attributes(projection.getAttributes())
                .createdAt(projection.getCreatedAt())
                .updatedAt(projection.getUpdatedAt())
                .build();
    }

    /**
     * Chuyển đổi từ ProductDocument (ES Document) sang ProductDetailProjection (Application DTO)
     */
    public static ProductDetailProjection toDetailProjection(ProductDocument doc) {
        if (doc == null) {
            return null;
        }
        
        UUID productId = UUID.fromString(doc.getId());
        
        List<ProductDetailProjection.Breadcrumb> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(ProductDetailProjection.Breadcrumb.builder().id("home").label("Trang chủ").build());
        breadcrumbList.add(ProductDetailProjection.Breadcrumb.builder()
                .id(doc.getCategorySlug() != null ? doc.getCategorySlug() : doc.getCategoryId())
                .label(doc.getCategoryName() != null ? doc.getCategoryName() : "Sản phẩm")
                .build());

        Map<String, String> attributesMap = new HashMap<>();
        if (doc.getAttributes() != null) {
            doc.getAttributes().forEach((k, v) -> attributesMap.put(k, v != null ? v.toString() : null));
        }

        List<String> tagsList = List.of("new", "sale");
        List<String> featuresList = List.of("Thiết kế hiện đại", "Chất liệu cao cấp", "Bảo hành 12 tháng");

        return ProductDetailProjection.builder()
                .id(productId)
                .slug(doc.getId())
                .shopId(doc.getShopId() != null ? UUID.fromString(doc.getShopId()) : null)
                .categoryId(doc.getCategoryId() != null ? UUID.fromString(doc.getCategoryId()) : null)
                .categoryName(doc.getCategoryName())
                .category(ProductDetailProjection.CategoryInfo.builder()
                        .id(doc.getCategorySlug() != null ? doc.getCategorySlug() : doc.getCategoryId())
                        .label(doc.getCategoryName())
                        .build())
                .name(doc.getName())
                .description(doc.getDescription())
                .thumbnailUrl(doc.getThumbnailUrl())
                .image(doc.getThumbnailUrl())
                .gallery(List.of(doc.getThumbnailUrl(), doc.getThumbnailUrl()))
                .rating(doc.getRating() != null ? doc.getRating() : 4.8)
                .ratingCount(120)
                .stock(doc.getTotalStock() != null ? doc.getTotalStock() : 0)
                .tags(tagsList)
                .materials(doc.getMaterials())
                .colors(doc.getColors())
                .sizes(doc.getSizes())
                .supports3d(true)
                .roomTypeHint(doc.getCategoryName() != null ? doc.getCategoryName() : "Room")
                .modelUrl("/models/sofa.glb")
                .breadcrumb(breadcrumbList)
                .features(featuresList)
                .status(doc.getStatus())
                .price(doc.getMinPrice())
                .build();
    }
}

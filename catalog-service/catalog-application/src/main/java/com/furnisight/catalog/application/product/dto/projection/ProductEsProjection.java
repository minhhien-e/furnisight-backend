package com.furnisight.catalog.application.product.dto.projection;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Projection đại diện cho dữ liệu Product lấy từ Elasticsearch ở lớp Application.
 * Giúp tránh vi phạm quy tắc phụ thuộc ngược từ Application sang Infrastructure.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEsProjection {
    private String id;
    private String name;
    private String description;
    private String status;
    private String categoryId;
    private String categoryName;
    private String categorySlug;
    private String shopId;
    private Double minPrice;
    private Double maxPrice;
    private List<String> colors;
    private List<String> materials;
    private List<String> sizes;
    private Integer totalStock;
    private Integer viewCount;
    private Double rating;
    private String thumbnailUrl;
    private Map<String, Object> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

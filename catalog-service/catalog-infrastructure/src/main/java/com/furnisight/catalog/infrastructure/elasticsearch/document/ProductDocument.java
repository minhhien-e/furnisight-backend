package com.furnisight.catalog.infrastructure.elasticsearch.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ES Document ánh xạ tới index "products" trong Elasticsearch.
 * Được đồng bộ từ PostgreSQL thông qua Kafka consumer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
@Setting(settingPath = "/elasticsearch/product-settings.json")
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String name;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String categorySlug;

    @Field(type = FieldType.Keyword)
    private String shopId;

    @Field(type = FieldType.Double)
    private Double minPrice;

    @Field(type = FieldType.Double)
    private Double maxPrice;

    @Field(type = FieldType.Keyword)
    private List<String> colors;

    @Field(type = FieldType.Keyword)
    private List<String> materials;

    @Field(type = FieldType.Keyword)
    private List<String> sizes;

    @Field(type = FieldType.Integer)
    private Integer totalStock;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Keyword)
    private String thumbnailUrl;

    @Field(type = FieldType.Object, enabled = false)
    private Map<String, Object> attributes;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;
}

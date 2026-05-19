package com.furnisight.review.infrastructure.elasticsearch.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * ES Document mapping cho reviews index trong Elasticsearch.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "reviews")
@Setting(settingPath = "/elasticsearch/review-settings.json")
public class ReviewDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Keyword)
    private String productId;

    @Field(type = FieldType.Keyword)
    private String orderItemId;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String content;

    @Field(type = FieldType.Integer)
    private Integer rating;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
}

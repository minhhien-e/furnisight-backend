package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import com.furnisight.catalog.domain.valueobjects.category.CategoryName;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import com.furnisight.catalog.domain.valueobjects.product.VariantSpecifications;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private CategoryName name;

    @Embedded
    private CategorySlug slug;

    private UUID parentId;

    @Column(name = "room_type_id")
    private UUID roomTypeId;

    @Column(name = "product_count")
    @Builder.Default
    private Integer productCount = 0;

    @Column(name = "visible", nullable = false)
    @Builder.Default
    private Boolean visible = true;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "icon_url")
    private String iconUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spec_template", columnDefinition = "jsonb")
    private VariantSpecifications specTemplate;

    public void incrementProductCount() {
        if (this.productCount == null) {
            this.productCount = 0;
        }
        this.productCount++;
    }

    public static Category create(CategoryName name, CategorySlug slug, UUID parentId, UUID roomTypeId) {
        return Category.builder()
                .name(name)
                .slug(slug)
                .parentId(parentId)
                .roomTypeId(roomTypeId)
                .build();
    }

    public void update(CategoryName name, CategorySlug slug, UUID parentId, UUID roomTypeId) {
        if (name != null) this.name = name;
        if (slug != null) this.slug = slug;
        this.parentId = parentId;
        this.roomTypeId = roomTypeId;
    }
}

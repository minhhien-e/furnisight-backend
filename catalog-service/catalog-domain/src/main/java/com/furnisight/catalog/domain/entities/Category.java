package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import com.furnisight.catalog.domain.valueobjects.category.CategoryName;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "product_count")
    @Builder.Default
    private Integer productCount = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "icon_url")
    private String iconUrl;

    public void incrementProductCount() {
        if (this.productCount == null) {
            this.productCount = 0;
        }
        this.productCount++;
    }

    public static Category create(CategoryName name, CategorySlug slug, UUID parentId) {
        return Category.builder()
                .name(name)
                .slug(slug)
                .parentId(parentId)
                .build();
    }

    public void update(CategoryName name, CategorySlug slug, UUID parentId) {
        if (name != null) this.name = name;
        if (slug != null) this.slug = slug;
        this.parentId = parentId;
    }
}

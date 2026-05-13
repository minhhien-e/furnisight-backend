package com.furnisight.catalog.domain.entities.category;

import com.furnisight.catalog.domain.events.category.CategoryCreateEvent;
import com.furnisight.catalog.domain.events.category.CategoryUpdateEvent;
import com.furnisight.catalog.domain.valueobjects.category.CategoryName;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false, length = 100))
    private CategoryName name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "slug", nullable = false, unique = true))
    private CategorySlug slug;

    private UUID parentId;

    private String path; // He thong tu tinh toan, khong de nguoi dung nhap

    public static Category create(CategoryName name, CategorySlug slug, UUID parentId, String path) {
        Category category = Category.builder()
            .name(name)
            .slug(slug)
            .parentId(parentId)
            .path(path)
            .build();

        category.registerEvent(CategoryCreateEvent.builder()
            .categoryId(category.getId())
            .name(name.getValue())
            .slug(slug.getValue())
            .parentId(parentId)
            .occurredAt(LocalDateTime.now())
            .build());

        return category;
    }

    public void update(CategoryName name, CategorySlug slug, UUID parentId, String path) {
        if (name != null) this.name = name;
        if (slug != null) this.slug = slug;
        this.parentId = parentId;
        if (path != null) this.path = path;

        registerEvent(CategoryUpdateEvent.builder()
            .categoryId(this.id)
            .name(this.name.getValue())
            .slug(this.slug.getValue())
            .parentId(this.parentId)
            .occurredAt(LocalDateTime.now())
            .build());
    }
}

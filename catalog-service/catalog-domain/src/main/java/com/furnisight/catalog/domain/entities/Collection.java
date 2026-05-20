package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "collections")
public class Collection extends AggregateRoot {
    @Id
    private UUID id ;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "slug", unique = true)
    private String slug;

    public Collection(String name, String slug, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.slug = slug;
        this.description = description;
    }
}

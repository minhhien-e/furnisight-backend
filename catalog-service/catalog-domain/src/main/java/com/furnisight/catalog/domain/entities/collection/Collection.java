package com.furnisight.catalog.domain.entities.collection;

import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "collections")
public class Collection extends AggregateRoot {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "slug", unique = true)
    private String slug;
}

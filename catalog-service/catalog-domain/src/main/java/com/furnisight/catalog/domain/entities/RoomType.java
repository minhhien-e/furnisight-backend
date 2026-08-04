package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "room_types")
public class RoomType extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "slug", unique = true)
    private String slug;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "media_id")
    private UUID mediaId;

    @Column(name = "visible", nullable = false)
    @Builder.Default
    private Boolean visible = true;

    public static RoomType create(String name, String slug, String description, String imageUrl, UUID mediaId) {
        RoomType roomType = RoomType.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .imageUrl(imageUrl)
                .mediaId(mediaId)
                .visible(true)
                .build();
        return roomType;
    }

    public void update(String name, String slug, String description, String imageUrl, UUID mediaId, Boolean visible) {
        if (name != null && !name.isBlank()) this.name = name;
        if (slug != null && !slug.isBlank()) this.slug = slug;
        this.description = description;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (mediaId != null) this.mediaId = mediaId;

        if (visible != null) this.visible = visible;

    }
}

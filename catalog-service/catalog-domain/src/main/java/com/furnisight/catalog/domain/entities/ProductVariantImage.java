package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.seedwork.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_variant_images")
public class ProductVariantImage extends BaseEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ProductVariant variant;

    @Column(name = "media_id")
    private UUID mediaId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "position")
    private Integer position;
}

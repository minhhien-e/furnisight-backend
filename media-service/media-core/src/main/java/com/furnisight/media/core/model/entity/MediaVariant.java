package com.furnisight.media.core.model.entity;

import com.furnisight.media.core.model.enums.VariantType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class MediaVariant extends BaseEntity {

    /**
     * Asset gốc mà variant này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private MediaAsset asset;

    /**
     * THUMBNAIL / RESIZED / TRANSCODED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VariantType variantType;

    /**
     * Kích thước (chủ yếu cho image)
     */
    private Integer width;
    private Integer height;

    /**
     * Định dạng output: jpg, png, webp, mp4
     */
    @Column(length = 20)
    private String format;

    /**
     * Key trên storage cho variant này
     */
    @Column(nullable = false, unique = true)
    private String storageKey;

    /**
     * Size variant (bytes)
     */
    private Long sizeBytes;
}

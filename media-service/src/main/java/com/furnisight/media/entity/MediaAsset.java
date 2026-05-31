package com.furnisight.media.entity;

import com.furnisight.media.enums.AssetState;
import com.furnisight.media.enums.MediaType;
import com.furnisight.media.enums.OwnerType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "media_assets",
    indexes = @Index(name = "idx_media_asset_owner", columnList = "owner_id, owner_type")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Cloudinary public_id để delete / transform */
    @Column(name = "cloudinary_public_id", nullable = false, unique = true)
    private String cloudinaryPublicId;

    /** URL công khai trả về từ Cloudinary */
    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    /** Secure URL (https) */
    @Column(name = "secure_url", nullable = false, length = 2048)
    private String secureUrl;

    /** Ai sở hữu asset */
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OwnerType ownerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private AssetState state;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "format", length = 20)
    private String format;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

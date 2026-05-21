package com.furnisight.media.core.model.entity;

import com.furnisight.media.core.model.enums.AssetState;
import com.furnisight.media.core.model.enums.MediaType;
import com.furnisight.media.core.model.enums.OwnerType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(indexes = {
    @Index(name = "idx_media_asset_owner", columnList = "owner_id, owner_type")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAsset extends BaseEntity {

    /**
     * Ai upload / sở hữu asset
     * (user_id, shop_id, service_id...)
     */
    @Column(nullable = false)
    private UUID ownerId;

    /**
     * Loại owner (User, Shop, Product...)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnerType ownerType;

    /**
     * Loại file
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    /**
     * Trạng thái xử lý
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetState state;

    /**
     * Key gốc trên storage (S3, GCS, MinIO)
     */
    @Column(nullable = false, unique = true)
    private String originalKey;

    /**
     * Tên file gốc do client upload
     */
    private String originalFilename;

    /**
     * MIME type: image/jpeg, video/mp4
     */
    @Column(length = 100)
    private String mimeType;

    /**
     * Size file gốc (bytes)
     */
    private Long sizeBytes;

    /**
     * Lý do từ chối (ví dụ: chứa virus)
     */
    @Column(length = 500)
    private String rejectReason;
}

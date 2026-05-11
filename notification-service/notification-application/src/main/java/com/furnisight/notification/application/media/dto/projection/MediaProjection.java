package com.furnisight.notification.application.media.dto.projection;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MediaProjection {
    private UUID mediaId;
    private String originalFilename;
    private String state;
    private String rejectReason;
}

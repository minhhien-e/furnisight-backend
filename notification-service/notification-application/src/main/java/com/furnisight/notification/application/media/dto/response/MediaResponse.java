package com.furnisight.notification.application.media.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MediaResponse {
    private UUID mediaId;
    private String originalFilename;
    private String state;
    private String rejectReason;
}

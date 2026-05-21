package com.furnisight.media.api.dto.response;

import com.furnisight.media.core.model.enums.AssetState;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class MediaStatusResponse {
    private UUID mediaId;
    private String originalFilename;
    private AssetState state;
    private String downloadUrl;
    private Map<String, String> variants;
    private String rejectReason;
}

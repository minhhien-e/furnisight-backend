package com.furnisight.media.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class InitUploadResponse {
    private UUID mediaId;
    private String uploadUrl;
    private String state;
}

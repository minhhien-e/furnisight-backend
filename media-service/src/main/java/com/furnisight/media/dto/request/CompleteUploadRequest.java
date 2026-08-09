package com.furnisight.media.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CompleteUploadRequest {
    @JsonProperty("asset_id")
    private String assetId;

    @JsonProperty("public_id")
    private String publicId;

    private Long version;
    private String signature;
    private String format;

    @JsonProperty("resource_type")
    private String resourceType;

    private Long bytes;
    private Integer width;
    private Integer height;

    @JsonProperty("original_filename")
    private String originalFilename;

    private String url;

    @JsonProperty("secure_url")
    private String secureUrl;
}

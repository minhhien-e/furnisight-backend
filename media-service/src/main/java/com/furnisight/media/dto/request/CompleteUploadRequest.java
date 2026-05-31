package com.furnisight.media.dto.request;

import lombok.Data;

@Data
public class CompleteUploadRequest {
    private String assetId;
    private String publicId;
    private Long version;
    private String signature;
    private String format;
    private String resourceType;
    private Long bytes;
    private Integer width;
    private Integer height;
    private String originalFilename;
    private String url;
    private String secureUrl;
}

package com.furnisight.media.dto.request;

import com.furnisight.media.enums.OwnerType;
import lombok.Data;

import java.util.UUID;

@Data
public class InitUploadRequest {
    private String contentType;
    private String fileName;
    private Long sizeBytes;
    private OwnerType ownerType;
    private UUID ownerId;
    private String folder;
}

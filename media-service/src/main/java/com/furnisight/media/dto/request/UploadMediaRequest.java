package com.furnisight.media.dto.request;

import com.furnisight.media.enums.OwnerType;
import lombok.Data;

import java.util.UUID;

@Data
public class UploadMediaRequest {
    private UUID ownerId;
    private OwnerType ownerType;
    /** Cloudinary folder (optional, e.g. "avatars", "products") */
    private String folder;
}

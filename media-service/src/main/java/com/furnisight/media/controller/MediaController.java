package com.furnisight.media.controller;

import com.furnisight.media.dto.request.UploadMediaRequest;
import com.furnisight.media.dto.response.MediaResponse;
import com.furnisight.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    /**
     * Upload file trực tiếp — multipart/form-data.
     * Part "file"    : the actual file
     * Part "ownerId" : UUID of the owner
     * Part "ownerType": USER | SHOP | PRODUCT | ...
     * Part "folder"  : (optional) Cloudinary folder
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<MediaResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("ownerId") String ownerId,
            @RequestPart("ownerType") String ownerType,
            @RequestPart(value = "folder", required = false) String folder) {

        UploadMediaRequest req = new UploadMediaRequest();
        req.setOwnerId(UUID.fromString(ownerId));
        req.setOwnerType(com.furnisight.media.enums.OwnerType.valueOf(ownerType.toUpperCase()));
        req.setFolder(folder);

        return ResponseEntity.ok(mediaService.upload(file, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mediaService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.furnisight.media.api.controller.rest;

import com.furnisight.media.api.dto.request.InitUploadRequest;
import com.furnisight.media.api.dto.response.InitUploadResponse;
import com.furnisight.media.api.dto.response.MediaStatusResponse;
import com.furnisight.media.api.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/init-upload")
    public ResponseEntity<InitUploadResponse> initUpload(@RequestBody InitUploadRequest request, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        request.setIdempotencyKey(idempotencyKey);
        return ResponseEntity.ok(mediaService.initUpload(request));
    }

    @PutMapping("/{mediaId}/complete-upload")
    public ResponseEntity<Void> completeUpload(@PathVariable UUID mediaId) {
        mediaService.completeUpload(mediaId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<MediaStatusResponse> getMediaStatus(@PathVariable UUID mediaId) {
        return ResponseEntity.ok(mediaService.getMediaById(mediaId));
    }
}

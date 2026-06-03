package com.furnisight.media.controller;

import com.furnisight.media.dto.request.CompleteUploadRequest;
import com.furnisight.media.dto.request.InitUploadRequest;
import com.furnisight.media.dto.response.InitUploadResponse;
import com.furnisight.media.dto.response.MediaResponse;
import com.furnisight.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/init-upload")
    public ResponseEntity<InitUploadResponse> initUpload(@RequestBody InitUploadRequest request) {
        return ResponseEntity.ok(mediaService.initUpload(request));
    }

    @PutMapping("/{mediaId}/complete-upload")
    public ResponseEntity<MediaResponse> completeUpload(
            @PathVariable UUID mediaId,
            @RequestBody CompleteUploadRequest request) {
        return ResponseEntity.ok(mediaService.completeUpload(mediaId, request));
    }

    @PostMapping("/{mediaId}/cancel-upload")
    public ResponseEntity<Void> cancelUpload(@PathVariable UUID mediaId) {
        mediaService.cancelUpload(mediaId);
        return ResponseEntity.noContent().build();
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

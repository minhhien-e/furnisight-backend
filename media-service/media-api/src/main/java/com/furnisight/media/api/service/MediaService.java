package com.furnisight.media.api.service;

import com.furnisight.media.api.dto.request.InitUploadRequest;
import com.furnisight.media.api.dto.response.InitUploadResponse;
import com.furnisight.media.api.dto.response.MediaStatusResponse;

import java.util.UUID;

public interface MediaService {
    InitUploadResponse initUpload(InitUploadRequest request);
    void completeUpload(UUID mediaId);
    MediaStatusResponse getMediaById(UUID mediaId);
}

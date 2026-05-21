package com.furnisight.media.infrastructure.util;

import com.furnisight.media.core.model.enums.MediaType;
import com.furnisight.media.core.detector.MediaTypeDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TikaMediaTypeDetector implements MediaTypeDetector {

    private final Tika tika = new Tika();

    @Override
    public MediaType detect(String contentType, String fileName) {
        String detectedMimeType = contentType;

        if (detectedMimeType == null || detectedMimeType.isBlank() || detectedMimeType.equals("application/octet-stream")) {
            if (fileName != null && !fileName.isBlank()) {
                detectedMimeType = tika.detect(fileName);
            }
        }

        if (detectedMimeType == null) {
            log.warn("Could not determine mime type for file: {}, defaulting to DOCUMENT", fileName);
            return MediaType.DOCUMENT;
        }

        detectedMimeType = detectedMimeType.toLowerCase();

        if (detectedMimeType.startsWith("image/")) {
            return MediaType.IMAGE;
        } else if (detectedMimeType.startsWith("video/")) {
            return MediaType.VIDEO;
        } else if (detectedMimeType.startsWith("audio/")) {
            return MediaType.AUDIO;
        } else {
            return MediaType.DOCUMENT;
        }
    }
}

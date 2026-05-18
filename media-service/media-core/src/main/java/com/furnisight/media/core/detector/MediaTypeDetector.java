package com.furnisight.media.core.detector;

import com.furnisight.media.core.model.enums.MediaType;

public interface MediaTypeDetector {
    MediaType detect(String contentType, String fileName);
}

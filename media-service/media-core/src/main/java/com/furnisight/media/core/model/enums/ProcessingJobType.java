package com.furnisight.media.core.model.enums;

public enum ProcessingJobType {
    INIT,
    /**
     * Security & validation
     */
    VIRUS_SCAN,
    FILE_SIGNATURE_VALIDATION,

    /**
     * Image processing
     */
    RESIZE_IMAGE,
    GENERATE_THUMBNAIL,

    /**
     * Video processing
     */
    TRANSCODE_VIDEO
}

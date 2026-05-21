package com.furnisight.media.infrastructure.utils;

import org.apache.tika.Tika;

import java.io.InputStream;

public final class MimeTypeDetector {

    private static final Tika TIKA = new Tika();

    private MimeTypeDetector() {}

    public static String detect(InputStream inputStream) {
        try {
            return TIKA.detect(inputStream);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to detect MIME type", e);
        }
    }
}

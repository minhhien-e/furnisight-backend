package com.furnisight.media.exception;

import java.util.UUID;

public class MediaNotFoundException extends RuntimeException {
    public MediaNotFoundException(UUID id) {
        super("Media not found: " + id);
    }
}

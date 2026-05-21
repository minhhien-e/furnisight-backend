package com.furnisight.media.api.controller.rest;

import com.furnisight.media.core.storage.StorageProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.HashMap;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaLocalUploadController {

    private final StorageProvider storageProvider;

    @PutMapping("/upload/{*key}")
    public ResponseEntity<Void> uploadFile(
            @PathVariable String key,
            @RequestParam("expires") long expires,
            @RequestParam("signature") String signature,
            HttpServletRequest request
    ) throws Exception {
        if (key.startsWith("/")) {
            key = key.substring(1);
        }

        storageProvider.upload(key, request.getInputStream(), request.getContentLengthLong(), new HashMap<>());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{*key}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String key,
            @RequestParam("expires") long expires,
            @RequestParam("signature") String signature
    ) {
        if (key.startsWith("/")) {
            key = key.substring(1);
        }

        InputStream is = storageProvider.download(key);
        InputStreamResource resource = new InputStreamResource(is);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}

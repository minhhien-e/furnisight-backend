package com.furnisight.media.infrastructure.storage.local;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class LocalStorageProperties {
    private Path directory;
    private String baseUrl;
}

package com.furnisight.media.infrastructure.antivirus;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.clamav")
public class ClamAvProperties {
    private String host = "localhost";
    private int port = 3310;
    private int timeoutMs = 10000;
}

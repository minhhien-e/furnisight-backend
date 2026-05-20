package com.furnisight.catalog.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.whitelist")
public class SecurityProperties {
    private List<String> publicAll = new ArrayList<>();
    private List<String> publicGet = new ArrayList<>();

    public String[] publicAllArray() {
        return publicAll.toArray(String[]::new);
    }

    public String[] publicGetArray() {
        return publicGet.toArray(String[]::new);
    }
}

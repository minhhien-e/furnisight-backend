package com.furnisight.catalog.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.whitelist")
public class SecurityProperties {
    private String[] publicAll = new String[] {};
    private String[] publicGet = new String[] {};
}

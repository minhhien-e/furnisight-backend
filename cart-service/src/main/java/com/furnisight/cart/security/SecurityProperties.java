package com.furnisight.cart.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.security.whitelist")
public class SecurityProperties {
    private String[] publicAll = {};
    private String[] publicGet = {};
}

package com.furnisight.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.whitelist")
public class SecurityWhitelistProperties {

    private String[] publicAll = new String[] {};
    private String[] authAll = new String[] {};
    private String[] passwordAll = new String[] {};
    private String[] oauthAll = new String[] {};
    private String[] publicGet = new String[] {};
}
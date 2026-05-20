package com.furnisight.gateway.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = "app.security.whitelist")
public class SecurityWhitelistProperties {

    private List<String> publicAll = new ArrayList<>();
    private List<String> authAll = new ArrayList<>();
    private List<String> passwordAll = new ArrayList<>();
    private List<String> oauthAll = new ArrayList<>();
    private List<String> publicGet = new ArrayList<>();

    public String[] publicAllArray() {
        return merge(publicAll, authAll, passwordAll, oauthAll);
    }

    public String[] publicGetArray() {
        return publicGet.toArray(String[]::new);
    }

    @SafeVarargs
    private String[] merge(List<String>... lists) {
        return Stream.of(lists)
                .flatMap(Collection::stream)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
    }

    public List<String> getPublicAll() {
        return publicAll;
    }

    public void setPublicAll(List<String> publicAll) {
        this.publicAll = publicAll;
    }

    public List<String> getAuthAll() {
        return authAll;
    }

    public void setAuthAll(List<String> authAll) {
        this.authAll = authAll;
    }

    public List<String> getPasswordAll() {
        return passwordAll;
    }

    public void setPasswordAll(List<String> passwordAll) {
        this.passwordAll = passwordAll;
    }

    public List<String> getOauthAll() {
        return oauthAll;
    }

    public void setOauthAll(List<String> oauthAll) {
        this.oauthAll = oauthAll;
    }

    public List<String> getPublicGet() {
        return publicGet;
    }

    public void setPublicGet(List<String> publicGet) {
        this.publicGet = publicGet;
    }
}
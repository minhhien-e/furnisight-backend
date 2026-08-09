package com.furnisight.user.infrastructure.security.handler;

import com.furnisight.user.infrastructure.security.entity.CustomOauth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        CustomOauth2User oauthUser = (CustomOauth2User) token.getPrincipal();

        var accountToken = oauthUser.accountToken();
        String accessToken = accountToken.getAccessToken().getValue();
        String refreshToken = accountToken.getRefreshToken().getValue();

        String redirectUrl = frontendUrl + "/auth/callback"
                + "?access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}

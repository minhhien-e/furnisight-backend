package com.furnisight.user.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomOAuth2UserService oauth2UserService,
                                                   CustomOidcUserService oidcUserService,
                                                   OAuth2SuccessHandler oauth2SuccessHandler,
                                                   OAuth2FailureHandler oauth2FailureHandler) throws Exception {

        var sessionRepo = new HttpSessionSecurityContextRepository();

        var authRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .securityContext(ctx -> ctx.securityContextRepository(sessionRepo))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers(securityProperties.getAuthWhitelist()).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .oauth2Login(oauth -> oauth
                .authorizationEndpoint(auth -> auth
                    .authorizationRequestRepository(authRequestRepository))
                .loginProcessingUrl("/login/oauth2/code/*")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauth2UserService)
                    .oidcUserService(oidcUserService))
                .successHandler(oauth2SuccessHandler)
                .failureHandler(oauth2FailureHandler)
            );
        return http.build();
    }

    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> permissions = jwt.getClaimAsStringList("permissions");

            if (permissions == null) return List.of();

            return permissions.stream()
                .map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        });

        return converter;
    }
}

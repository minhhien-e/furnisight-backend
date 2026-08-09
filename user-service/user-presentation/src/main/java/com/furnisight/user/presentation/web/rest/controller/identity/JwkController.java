package com.furnisight.user.presentation.web.rest.controller.identity;

import com.furnisight.user.application.common.port.in.JWKProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwkController {
    private final JWKProvider jwkProvider;

    @GetMapping("auth/realms/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        return jwkProvider.getJWK();
    }
}

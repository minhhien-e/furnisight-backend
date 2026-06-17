package com.furnisight.user.infrastructure.auth.generator;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.enums.identity.Permission;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.services.identity.generator.AccessTokenGenerator;
import com.furnisight.user.domain.valueobjects.identity.AccessToken;
import com.furnisight.user.infrastructure.security.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenGenerator implements AccessTokenGenerator {

    private final JwtEncoder jwtEncoder;
    private final JwtConfig jwtConfig;
    private final RoleRepository roleRepository;

    @Override
    public AccessToken generateToken(Account account) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtConfig.getAccessTokenExpirationMs());

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        List<Role> roles = roleRepository.findAllByAccountId(account.getId());

        List<String> permissionNames = getPermissions(account, roles).stream()
            .map(Permission::name).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(account.getId().toString())
            .claim("roles", roles.stream().map(role -> role.getName().getValue()).toList())
            .claim("permissions", permissionNames)
            .claim("isAdmin", !permissionNames.isEmpty())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .build();

        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        LocalDateTime expirationDateTime = expiresAt.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return new AccessToken(tokenValue, expirationDateTime, false);
    }

    private Set<Permission> getPermissions(Account account, List<Role> roles) {
        if (account.isBanned() || account.isLocked()) return Set.of();
        return roles.stream().flatMap(role -> role.getPermissions().stream()).collect(Collectors.toSet());
    }
}

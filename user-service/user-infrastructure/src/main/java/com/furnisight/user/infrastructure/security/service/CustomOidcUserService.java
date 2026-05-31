package com.furnisight.user.infrastructure.security.service;

import com.furnisight.user.application.account.dto.LoginWithSocialAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.LoginWithSocialAccountUseCase;
import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.enums.identity.SocialProvider;
import com.furnisight.user.infrastructure.security.entity.CustomOauth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final LoginWithSocialAccountUseCase loginWithSocialAccountUseCase;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(request);

        SocialProvider provider = SocialProvider.valueOf(
                request.getClientRegistration().getRegistrationId().toUpperCase());

        String providerUserId = oidcUser.getAttribute("sub");
        String email          = oidcUser.getAttribute("email");
        String fullName       = oidcUser.getAttribute("name");
        String firstName      = oidcUser.getAttribute("given_name");
        String lastName       = oidcUser.getAttribute("family_name");
        String picture        = oidcUser.getAttribute("picture");

        LoginWithSocialAccountCommand command = new LoginWithSocialAccountCommand(
                providerUserId, provider, email, picture, fullName, firstName, lastName
        );

        AccountToken token = loginWithSocialAccountUseCase.execute(command);
        return new CustomOauth2User(oidcUser, token);
    }
}

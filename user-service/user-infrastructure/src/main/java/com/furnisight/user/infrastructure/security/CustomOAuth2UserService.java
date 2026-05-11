package com.furnisight.user.infrastructure.security;

import com.furnisight.user.application.account.dto.LoginWithSocialAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.LoginWithSocialAccountUseCase;
import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.enums.identity.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final LoginWithSocialAccountUseCase loginWithSocialAccountUseCase;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(request);

        SocialProvider provider = SocialProvider.valueOf(
                request.getClientRegistration().getRegistrationId().toUpperCase());

        String providerUserId = oauthUser.getAttribute("sub");
        String email          = oauthUser.getAttribute("email");
        String name           = oauthUser.getAttribute("name");
        String picture        = oauthUser.getAttribute("picture");

        LoginWithSocialAccountCommand command = new LoginWithSocialAccountCommand(
                providerUserId, provider, email, picture, name, null, null
        );

        AccountToken token = loginWithSocialAccountUseCase.execute(command);
        return new CustomOauth2User(oauthUser, token);
    }
}

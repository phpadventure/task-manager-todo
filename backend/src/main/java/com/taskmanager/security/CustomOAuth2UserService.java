package com.taskmanager.security;

import com.taskmanager.dto.OAuthUserInfo;
import com.taskmanager.entity.User;
import com.taskmanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final AuthService authService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
        throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthUserInfo oauthUserInfo = extractOAuthUserInfo(
            registrationId, oAuth2User.getAttributes());
        User user = authService.findOrCreateUserFromOAuth(oauthUserInfo);

        return new CustomOAuth2User(user);
    }

    private OAuthUserInfo extractOAuthUserInfo(
        String registrationId, java.util.Map<String, Object> attributes) {
        OAuthUserInfo info = new OAuthUserInfo();
        info.setProvider(registrationId);

        if ("google".equals(registrationId)) {
            info.setProviderUserId((String) attributes.get("id"));
            info.setEmail((String) attributes.get("email"));
            info.setDisplayName((String) attributes.get("name"));
            info.setAvatarUrl((String) attributes.get("picture"));
        } else if ("github".equals(registrationId)) {
            info.setProviderUserId(String.valueOf(attributes.get("id")));
            info.setEmail((String) attributes.get("email"));
            info.setDisplayName((String) attributes.get("login"));
            info.setAvatarUrl((String) attributes.get("avatar_url"));
        }

        return info;
    }
}

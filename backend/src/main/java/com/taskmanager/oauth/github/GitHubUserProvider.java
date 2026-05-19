package com.taskmanager.oauth.github;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GitHubUserProvider {
    private final GitHubEmailService githubEmailService;

    public void enrichUserAttributesWithEmail(OAuth2UserRequest userRequest,
        Map<String, Object> attributes) throws OAuth2AuthenticationException {
        if (attributes.get("email") == null) {
            log.debug("GitHub user email is null, attempting to fetch from /user/emails endpoint");
            String accessToken = userRequest.getAccessToken().getTokenValue();
            String email = githubEmailService.fetchPrimaryEmailFromGitHub(accessToken);

            if (email == null) {
                throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email"),
                    "Unable to retrieve email from GitHub. " +
                    "Please ensure your email is verified and not private in GitHub settings."
                );
            }

            attributes.put("email", email);
            log.debug("Successfully retrieved email from GitHub /user/emails endpoint");
        }
    }
}

package com.taskmanager.oauth.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@ExtendWith(MockitoExtension.class)
class GitHubUserProviderTest {
    @Mock
    private GitHubEmailService githubEmailService;

    private GitHubUserProvider provider;

    @BeforeEach
    void setUp() {
        provider = new GitHubUserProvider(githubEmailService);
    }

    @Test
    void testEnrichUserAttributesWithEmail_WithEmailNull_FetchesFromApi() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", null);
        attributes.put("id", "123456");

        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(userRequest.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("test-token");
        when(githubEmailService.fetchPrimaryEmailFromGitHub("test-token"))
            .thenReturn("fetched@example.com");

        provider.enrichUserAttributesWithEmail(userRequest, attributes);

        assertThat(attributes.get("email")).isEqualTo("fetched@example.com");
    }

    @Test
    void testEnrichUserAttributesWithEmail_WithEmailPresent_DoesNotFetch() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "existing@example.com");

        provider.enrichUserAttributesWithEmail(null, attributes);

        assertThat(attributes.get("email")).isEqualTo("existing@example.com");
    }

    @Test
    void testEnrichUserAttributesWithEmail_CannotFetchEmail_ThrowsException() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", null);

        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(userRequest.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("test-token");
        when(githubEmailService.fetchPrimaryEmailFromGitHub("test-token"))
            .thenReturn(null);

        assertThatThrownBy(() -> {
            provider.enrichUserAttributesWithEmail(userRequest, attributes);
        }).isInstanceOf(OAuth2AuthenticationException.class);
    }
}

package com.taskmanager.service;

import com.taskmanager.dto.OAuthUserInfo;
import com.taskmanager.entity.User;
import com.taskmanager.entity.UserAuthProvider;
import com.taskmanager.exception.ConflictException;
import com.taskmanager.repository.UserAuthProviderRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthProviderRepository userAuthProviderRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void testFindOrCreateUserFromOAuth_WithExistingProvider_ReturnsUser() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "test@example.com");
        UserAuthProvider provider = createTestProvider(UUID.randomUUID(), user, "google", "google-123");

        when(userAuthProviderRepository.findByProviderAndProviderUserId("google", "google-123"))
            .thenReturn(Optional.of(provider));

        OAuthUserInfo oauthInfo = new OAuthUserInfo("google", "google-123", "test@example.com", "Test User", "avatar.jpg");
        User result = authService.findOrCreateUserFromOAuth(oauthInfo);

        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository, never()).save(any());
        verify(userAuthProviderRepository, never()).save(any());
    }

    @Test
    void testFindOrCreateUserFromOAuth_WithNewProvider_ExistingEmail_LinksProvider() {
        UUID userId = UUID.randomUUID();
        User existingUser = createTestUser(userId, "test@example.com");

        when(userAuthProviderRepository.findByProviderAndProviderUserId("google", "google-123"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(existingUser));

        OAuthUserInfo oauthInfo = new OAuthUserInfo("google", "google-123", "test@example.com", "Test User", "avatar.jpg");
        User result = authService.findOrCreateUserFromOAuth(oauthInfo);

        assertThat(result.getId()).isEqualTo(userId);
        ArgumentCaptor<UserAuthProvider> captor = ArgumentCaptor.forClass(UserAuthProvider.class);
        verify(userAuthProviderRepository).save(captor.capture());
        assertThat(captor.getValue().getProvider()).isEqualTo("google");
        assertThat(captor.getValue().getProviderUserId()).isEqualTo("google-123");
    }

    @Test
    void testFindOrCreateUserFromOAuth_WithNewUser_CreatesUserAndProvider() {
        when(userAuthProviderRepository.findByProviderAndProviderUserId("github", "github-456"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com"))
            .thenReturn(Optional.empty());

        UUID newUserId = UUID.randomUUID();
        User newUser = createTestUser(newUserId, "newuser@example.com");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        OAuthUserInfo oauthInfo = new OAuthUserInfo("github", "github-456", "newuser@example.com", "New User", "avatar.jpg");
        User result = authService.findOrCreateUserFromOAuth(oauthInfo);

        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        verify(userRepository).save(any(User.class));
        verify(userAuthProviderRepository).save(any(UserAuthProvider.class));
    }

    @Test
    void testLinkOAuthProvider_WithNewProvider_LinksSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "test@example.com");

        when(userAuthProviderRepository.findByUserIdAndProvider(userId, "github"))
            .thenReturn(Optional.empty());

        OAuthUserInfo oauthInfo = new OAuthUserInfo("github", "github-456", "test@example.com", "Test User", "avatar.jpg");
        authService.linkOAuthProvider(user, oauthInfo);

        ArgumentCaptor<UserAuthProvider> captor = ArgumentCaptor.forClass(UserAuthProvider.class);
        verify(userAuthProviderRepository).save(captor.capture());
        assertThat(captor.getValue().getProvider()).isEqualTo("github");
        assertThat(captor.getValue().getProviderUserId()).isEqualTo("github-456");
    }

    @Test
    void testLinkOAuthProvider_WithExistingProvider_ThrowsException() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "test@example.com");
        UserAuthProvider existingProvider = createTestProvider(UUID.randomUUID(), user, "github", "github-456");

        when(userAuthProviderRepository.findByUserIdAndProvider(userId, "github"))
            .thenReturn(Optional.of(existingProvider));

        OAuthUserInfo oauthInfo = new OAuthUserInfo("github", "github-456", "test@example.com", "Test User", "avatar.jpg");

        assertThatThrownBy(() -> authService.linkOAuthProvider(user, oauthInfo))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void testHasProvider_WithExistingProvider_ReturnsTrue() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "test@example.com");
        UserAuthProvider provider = createTestProvider(UUID.randomUUID(), user, "google", "google-123");

        when(userAuthProviderRepository.findByUserIdAndProvider(userId, "google"))
            .thenReturn(Optional.of(provider));

        boolean result = authService.hasProvider(userId, "google");

        assertThat(result).isTrue();
    }

    @Test
    void testHasProvider_WithNonExistingProvider_ReturnsFalse() {
        UUID userId = UUID.randomUUID();

        when(userAuthProviderRepository.findByUserIdAndProvider(userId, "google"))
            .thenReturn(Optional.empty());

        boolean result = authService.hasProvider(userId, "google");

        assertThat(result).isFalse();
    }

    private User createTestUser(UUID id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setDisplayName("Test User");
        user.setAvatarUrl("avatar.jpg");
        user.setCreatedAt(java.time.Instant.now());
        user.setUpdatedAt(java.time.Instant.now());
        return user;
    }

    private UserAuthProvider createTestProvider(UUID id, User user, String provider, String providerUserId) {
        UserAuthProvider authProvider = new UserAuthProvider();
        authProvider.setId(id);
        authProvider.setUser(user);
        authProvider.setProvider(provider);
        authProvider.setProviderUserId(providerUserId);
        authProvider.setCreatedAt(java.time.Instant.now());
        return authProvider;
    }
}

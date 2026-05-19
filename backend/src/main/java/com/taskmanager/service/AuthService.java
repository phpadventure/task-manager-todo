package com.taskmanager.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.taskmanager.dto.OAuthUserInfo;
import com.taskmanager.entity.User;
import com.taskmanager.entity.UserAuthProvider;
import com.taskmanager.exception.ConflictException;
import com.taskmanager.repository.UserAuthProviderRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;

    public User findOrCreateUserFromOAuth(OAuthUserInfo oauthUserInfo) {
        Optional<UserAuthProvider> existingProvider = userAuthProviderRepository
            .findByProviderAndProviderUserId(
                oauthUserInfo.getProvider(), oauthUserInfo.getProviderUserId());

        if (existingProvider.isPresent()) {
            return existingProvider.get().getUser();
        }

        Optional<User> existingUser = userRepository.findByEmail(oauthUserInfo.getEmail());

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = new User();
            user.setEmail(oauthUserInfo.getEmail());
            user.setDisplayName(oauthUserInfo.getDisplayName());
            user.setAvatarUrl(oauthUserInfo.getAvatarUrl());
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            user = userRepository.save(user);
        }

        UserAuthProvider authProvider = new UserAuthProvider();
        authProvider.setUser(user);
        authProvider.setUserId(user.getId());
        authProvider.setProvider(oauthUserInfo.getProvider());
        authProvider.setProviderUserId(oauthUserInfo.getProviderUserId());
        authProvider.setCreatedAt(Instant.now());
        userAuthProviderRepository.save(authProvider);

        return user;
    }

    public void linkOAuthProvider(User user, OAuthUserInfo oauthUserInfo) {
        Optional<UserAuthProvider> existing = userAuthProviderRepository
            .findByUserIdAndProvider(user.getId(), oauthUserInfo.getProvider());

        if (existing.isPresent()) {
            String message = "Provider " + oauthUserInfo.getProvider() + " already linked";
            throw new ConflictException(message);
        }

        UserAuthProvider authProvider = new UserAuthProvider();
        authProvider.setUser(user);
        authProvider.setUserId(user.getId());
        authProvider.setProvider(oauthUserInfo.getProvider());
        authProvider.setProviderUserId(oauthUserInfo.getProviderUserId());
        authProvider.setCreatedAt(Instant.now());
        userAuthProviderRepository.save(authProvider);
    }

    public boolean hasProvider(UUID userId, String provider) {
        return userAuthProviderRepository.findByUserIdAndProvider(userId, provider).isPresent();
    }
}

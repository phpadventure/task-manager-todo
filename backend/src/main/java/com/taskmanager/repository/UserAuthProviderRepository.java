package com.taskmanager.repository;

import java.util.Optional;
import java.util.UUID;

import com.taskmanager.entity.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, UUID> {
    Optional<UserAuthProvider> findByProviderAndProviderUserId(
        String provider, String providerUserId);

    Optional<UserAuthProvider> findByUserIdAndProvider(UUID userId, String provider);
}

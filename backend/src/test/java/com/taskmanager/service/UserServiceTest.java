package com.taskmanager.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.taskmanager.dto.UserDTO;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetUserById_WithExistingUser_ReturnsUserDTO() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "test@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testGetUserById_WithNonExistingUser_ThrowsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGetUserByEmail_WithExistingUser_ReturnsUserDTO() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserByEmail("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testGetUserByEmail_WithNonExistingUser_ThrowsException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@example.com"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    private User createTestUser(UUID id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setDisplayName("Test User");
        user.setAvatarUrl("avatar.jpg");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }
}

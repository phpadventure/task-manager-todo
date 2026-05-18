package com.taskmanager.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {
    @Mock
    private UserRepository userRepository;

    @Test
    void testUserRepositoryExists() {
        assertThat(userRepository).isNotNull();
    }
}

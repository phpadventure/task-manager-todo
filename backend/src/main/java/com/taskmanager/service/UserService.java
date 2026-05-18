package com.taskmanager.service;

import java.util.UUID;

import com.taskmanager.dto.UserDTO;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnauthorizedException;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return UserDTO.fromEntity(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return UserDTO.fromEntity(user);
    }

    public UserDTO updateUser(UUID id, UserDTO dto) {
        verifyOwnership(id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setDisplayName(dto.getDisplayName());
        user.setAvatarUrl(dto.getAvatarUrl());
        User saved = userRepository.save(user);
        return UserDTO.fromEntity(saved);
    }

    public User getUserEntityById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void verifyOwnership(UUID resourceUserId) {
        UUID currentUserId = getCurrentUser().getId();
        if (!resourceUserId.equals(currentUserId)) {
            throw new UnauthorizedException("You do not have access to this resource");
        }
    }

    private SecurityUser getCurrentUser() {
        return (SecurityUser) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
    }
}

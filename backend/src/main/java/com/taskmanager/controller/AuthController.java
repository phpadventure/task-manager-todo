package com.taskmanager.controller;

import com.taskmanager.dto.UserProfileResponse;
import com.taskmanager.security.CustomOAuth2User;
import com.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            var user = oAuth2User.getUser();
            return ResponseEntity.ok(UserProfileResponse.fromEntity(user));
        } catch (ClassCastException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}

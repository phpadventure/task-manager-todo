package com.taskmanager.controller;

import com.taskmanager.dto.ErrorResponse;
import com.taskmanager.dto.UserProfileResponse;
import com.taskmanager.security.CustomOAuth2User;
import com.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OAuth2 login and profile endpoints")
public class AuthController {
    private final UserService userService;

    @Operation(
        summary = "Get current authenticated user",
        description = "Returns the profile of the currently authenticated user "
            + "(email, name, avatar)")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(
        summary = "Logout current user",
        description = "Invalidates the session and clears authentication context")
    @ApiResponse(
        responseCode = "200",
        description = "Successfully logged out")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}

package com.taskmanager.dto;

import java.util.UUID;

import com.taskmanager.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String displayName;
    private String avatarUrl;

    public static UserProfileResponse fromEntity(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getAvatarUrl()
        );
    }
}

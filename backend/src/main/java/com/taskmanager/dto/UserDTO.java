package com.taskmanager.dto;

import java.time.Instant;
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
public class UserDTO {
    private UUID id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private Instant createdAt;

    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getAvatarUrl(),
            user.getCreatedAt()
        );
    }
}

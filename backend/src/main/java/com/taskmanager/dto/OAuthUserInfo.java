package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthUserInfo {
    private String provider;
    private String providerUserId;
    private String email;
    private String displayName;
    private String avatarUrl;
}

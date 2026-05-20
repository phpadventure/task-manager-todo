package com.taskmanager.service;

import java.util.UUID;

import com.taskmanager.exception.UnauthorizedException;
import com.taskmanager.security.CustomOAuth2User;
import com.taskmanager.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityContextService {

  public UUID getCurrentUserId() {
    var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof SecurityUser securityUser) {
      return securityUser.getId();
    } else if (principal instanceof CustomOAuth2User oAuth2User) {
      return oAuth2User.getUser().getId();
    }

    throw new UnauthorizedException("User not authenticated");
  }
}

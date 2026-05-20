package com.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import com.taskmanager.entity.User;
import com.taskmanager.exception.UnauthorizedException;
import com.taskmanager.security.CustomOAuth2User;
import com.taskmanager.security.SecurityUser;
import com.taskmanager.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityContextServiceTest {

  private SecurityContextService securityContextService;

  @BeforeEach
  void setUp() {
    securityContextService = new SecurityContextService();
  }

  @Test
  void testGetCurrentUserId_WithSecurityUser_ReturnsUserId() {
    UUID userId = UUID.randomUUID();
    User user = TestDataBuilder.createTestUser(userId, "test@example.com");
    SecurityUser securityUser = new SecurityUser(user);

    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            securityUser, null, securityUser.getAuthorities());
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);

    UUID result = securityContextService.getCurrentUserId();

    assertThat(result).isEqualTo(userId);
  }

  @Test
  void testGetCurrentUserId_WithCustomOAuth2User_ReturnsUserId() {
    UUID userId = UUID.randomUUID();
    User user = TestDataBuilder.createTestUser(userId, "test@example.com");
    CustomOAuth2User oAuth2User = new CustomOAuth2User(user);

    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(oAuth2User);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);

    UUID result = securityContextService.getCurrentUserId();

    assertThat(result).isEqualTo(userId);
  }

  @Test
  void testGetCurrentUserId_WithUnknownPrincipal_ThrowsUnauthorizedException() {
    String unknownPrincipal = "unknownPrincipal";

    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(unknownPrincipal);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);

    assertThatThrownBy(() -> securityContextService.getCurrentUserId())
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("User not authenticated");
  }
}

package com.taskmanager.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
  @Value("${app.frontend.url}")
  private String frontendUrl;

  @Value("${app.frontend.redirect-path:/home}")
  private String redirectPath;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication)
      throws IOException, ServletException {
    String redirectUrl = frontendUrl + redirectPath;
    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }
}

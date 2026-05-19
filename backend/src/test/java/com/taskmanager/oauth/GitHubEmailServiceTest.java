package com.taskmanager.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class GitHubEmailServiceTest {
    @Mock
    private RestTemplate restTemplate;

    private GitHubEmailService service;

    @BeforeEach
    void setUp() {
        service = new GitHubEmailService(restTemplate, "https://api.github.com/user/emails");
    }

    @Test
    void testFetchPrimaryEmailFromGitHub_WithPrimaryVerifiedEmail_ReturnsEmail() {
        Map<String, Object>[] emails = new Map[]{
            Map.of("email", "primary@example.com", "primary", true, "verified", true),
            Map.of("email", "secondary@example.com", "primary", false, "verified", true)
        };

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map[].class)
        )).thenReturn(new ResponseEntity<>(emails, HttpStatus.OK));

        String result = service.fetchPrimaryEmailFromGitHub("test-token");

        assertThat(result).isEqualTo("primary@example.com");
    }

    @Test
    void testFetchPrimaryEmailFromGitHub_WithUnverifiedPrimary_SkipsAndReturnsNull() {
        Map<String, Object>[] emails = new Map[]{
            Map.of("email", "unverified@example.com", "primary", true, "verified", false),
            Map.of("email", "secondary@example.com", "primary", false, "verified", true)
        };

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map[].class)
        )).thenReturn(new ResponseEntity<>(emails, HttpStatus.OK));

        String result = service.fetchPrimaryEmailFromGitHub("test-token");

        assertThat(result).isNull();
    }

    @Test
    void testFetchPrimaryEmailFromGitHub_WithEmptyResponse_ReturnsNull() {
        Map<String, Object>[] emails = new Map[]{};

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map[].class)
        )).thenReturn(new ResponseEntity<>(emails, HttpStatus.OK));

        String result = service.fetchPrimaryEmailFromGitHub("test-token");

        assertThat(result).isNull();
    }

    @Test
    void testFetchPrimaryEmailFromGitHub_WithApiException_ReturnsNull() {
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map[].class)
        )).thenThrow(new RuntimeException("API Error"));

        String result = service.fetchPrimaryEmailFromGitHub("test-token");

        assertThat(result).isNull();
    }

    @Test
    void testFetchPrimaryEmailFromGitHub_WithNullResponse_ReturnsNull() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Map[]> nullResponse = mock(ResponseEntity.class);
        when(nullResponse.getBody()).thenReturn(null);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map[].class)
        )).thenReturn(nullResponse);

        String result = service.fetchPrimaryEmailFromGitHub("test-token");

        assertThat(result).isNull();
    }
}

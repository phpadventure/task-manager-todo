package com.taskmanager.oauth.github;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GitHubEmailService {
    private final RestTemplate restTemplate;
    private final String githubEmailsApiUrl;

    public GitHubEmailService(
        RestTemplate restTemplate,
        @Value("${github.api.emails-url}") String githubEmailsApiUrl) {
        this.restTemplate = restTemplate;
        this.githubEmailsApiUrl = githubEmailsApiUrl;
    }

    public String fetchPrimaryEmailFromGitHub(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map[]> response = restTemplate.exchange(
                githubEmailsApiUrl,
                HttpMethod.GET,
                request,
                Map[].class
            );

            Map[] emails = response.getBody();
            if (emails == null || emails.length == 0) {
                log.warn("GitHub /user/emails returned empty response");
                return null;
            }

            for (Map<String, Object> email : emails) {
                Boolean isPrimary = (Boolean) email.get("primary");
                Boolean isVerified = (Boolean) email.get("verified");

                if (Boolean.TRUE.equals(isPrimary) && Boolean.TRUE.equals(isVerified)) {
                    String emailValue = (String) email.get("email");
                    log.debug("Found primary verified email from GitHub: {}", emailValue);
                    return emailValue;
                }
            }

            log.warn("No primary verified email found in GitHub /user/emails response");
            return null;

        } catch (Exception e) {
            log.error("Failed to fetch email from GitHub /user/emails endpoint", e);
            return null;
        }
    }
}

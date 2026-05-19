package com.taskmanager.config;

import com.taskmanager.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;


import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final CustomOAuth2UserService oAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/me").permitAll()
                .requestMatchers("/api/seed").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/", "/index.html", "/*.js", "/*.css").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/", true)
                // .tokenEndpoint(token -> token
                //     .accessTokenResponseClient(accessTokenResponseClient())
                // )
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**");
    }

    // @Bean
    // public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {

    //     RestClientAuthorizationCodeTokenResponseClient tokenResponseClient = 
    //         new RestClientAuthorizationCodeTokenResponseClient();

    //     RestClient restClient = RestClient.builder()
    //             .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
    //             .requestInterceptor((request, body, execution) -> {
    //                 ClientHttpResponse response = execution.execute(request, body);
                    
    //                 // Read the full body content safely into bytes
    //                 byte[] responseBytes;
    //                 try (InputStream is = response.getBody()) {
    //                     responseBytes = is.readAllBytes();
    //                 }

    //                 String rawJsonBody = new String(responseBytes, StandardCharsets.UTF_8);
    //                 log.debug("\n=== RAW AUTH RESPONSE BODY ===\n{}\n================================\n", rawJsonBody);

    //                 // Create a cleanly wrapped clone so Spring Security can read the body from scratch
    //                 return new ClientHttpResponse() {
    //                     @Override
    //                     public HttpStatusCode getStatusCode() throws java.io.IOException {
    //                         return response.getStatusCode();
    //                     }

    //                     @Override
    //                     public String getStatusText() throws java.io.IOException {
    //                         return response.getStatusText();
    //                     }

    //                     @Override
    //                     public void close() {
    //                         response.close();
    //                     }

    //                     @Override
    //                     public InputStream getBody() throws java.io.IOException {
    //                         return new ByteArrayInputStream(responseBytes); // Fresh stream read pointer
    //                     }

    //                     @Override
    //                     public HttpHeaders getHeaders() {
    //                         return response.getHeaders();
    //                     }
    //                 };
    //             })
    //             .build();

    //     tokenResponseClient.setRestClient(restClient);
    //     return tokenResponseClient;

    // }
}

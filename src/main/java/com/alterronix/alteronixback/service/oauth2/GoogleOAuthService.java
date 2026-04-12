package com.alterronix.alteronixback.service.oauth2;

import com.alterronix.alteronixback.dto.GoogleTokenResponse;
import com.alterronix.alteronixback.entity.OAuthToken;
import com.alterronix.alteronixback.entity.User;
import com.alterronix.alteronixback.repository.OAuthTokenRepository;
import com.alterronix.alteronixback.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Service
public class GoogleOAuthService {

    private final String clientId;

    private final String clientSecret;

    private final String redirectUri;

    private final WebClient webClient;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final UserRepository userRepository;

    public GoogleOAuthService (
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret}") String clientSecret,
            @Value("${spring.security.oauth2.client.registration.google.redirect-uri}") String redirectUri,
            OAuthTokenRepository oAuthTokenRepository,
            UserRepository userRepository,
            @Qualifier("oAuth2WebClient") WebClient webClient
    ){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.userRepository = userRepository;
        this.webClient = webClient;
    }

    public OAuthToken exchangeCode(String code, User user) {

        GoogleTokenResponse response = webClient.post()
                .uri("/token")
                .bodyValue("""
                    {
                      "code": "%s",
                      "client_id": "%s",
                      "client_secret": "%s",
                      "redirect_uri": "%s",
                      "grant_type": "authorization_code"
                    }
                """.formatted(code, clientId, clientSecret, redirectUri))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        OAuthToken token = new OAuthToken();
        token.setUser(user);
        token.setAccessToken(response.access_token());
        token.setRefreshToken(response.refresh_token());
        token.setExpiresAt(Instant.now().plusSeconds(response.expires_in()));

        return oAuthTokenRepository.save(token);
    }

    public String refreshAccessToken(OAuthToken token) {

        if (token.getExpiresAt().isAfter(Instant.now().plusSeconds(60))) {
            return token.getAccessToken();
        }

        GoogleTokenResponse response = webClient.post()
                .uri("/token")
                .bodyValue("""
                {
                  "client_id": "%s",
                  "client_secret": "%s",
                  "refresh_token": "%s",
                  "grant_type": "refresh_token"
                }
            """.formatted(clientId, clientSecret, token.getRefreshToken()))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        token.setAccessToken(response.access_token());
        token.setExpiresAt(Instant.now().plusSeconds(response.expires_in()));

        oAuthTokenRepository.save(token);

        return token.getAccessToken();
    }
}

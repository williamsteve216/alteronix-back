package com.alterronix.alteronixback.service.oauth2;

import com.alterronix.alteronixback.entity.OAuthToken;
import com.alterronix.alteronixback.model.CustomUserPrincipal;
import com.alterronix.alteronixback.repository.OAuthTokenRepository;
import com.alterronix.alteronixback.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    private final OAuthTokenRepository oAuthTokenRepository;
    private final OAuth2AuthorizedClientService clientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        CustomUserPrincipal userPrincipal =
                (CustomUserPrincipal) authentication.getPrincipal();

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );

        String accessToken = jwtUtil.generateToken(userPrincipal.getUser().getEmail());
        String refreshToken = jwtUtil.generateRefreshToken();

        Instant expiresAt = client.getAccessToken().getExpiresAt();
        String googleAccessToken = client.getAccessToken().getTokenValue();
        String googleRefreshToken = client.getRefreshToken() != null
                ? client.getRefreshToken().getTokenValue()
                : null;

        // sauvegarder en DB
        OAuthToken token = oAuthTokenRepository.findByUser(userPrincipal.getUser())
                .orElse(new OAuthToken());
        token.setUser(userPrincipal.getUser());
        token.setAccessToken(googleAccessToken);
        token.setRefreshToken(googleRefreshToken);
        token.setExpiresAt(expiresAt);

        oAuthTokenRepository.save(token);

        // Option 1 : cookie sécurisé
        Cookie cookie = new Cookie("access_token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        // redirection frontend
        getRedirectStrategy().sendRedirect(
                request, response, "http://localhost:3000/dashboard"
        );
    }
}

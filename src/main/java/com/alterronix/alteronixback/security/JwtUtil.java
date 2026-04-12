package com.alterronix.alteronixback.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
@Slf4j
public class JwtUtil {
    private final SecretKey key;
    private final long expiration = 60 * 60 * 1000; // 1h

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public String generateToken(String email) {
        long expirationMillis = System.currentTimeMillis() + expiration;
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (io.jsonwebtoken.JwtException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }catch (Exception e) {
            throw new RuntimeException("An error occurred while parsing the JWT token", e);
        }
    }

    public String extractEmail(String token) {
        return extractClaims(token)
                .getSubject();
    }

    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof List<?> roleList) {
            return roleList.stream()
                    .filter(role -> role instanceof String)
                    .map(role -> (String) role)
                    .toList();
        }
        return List.of();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

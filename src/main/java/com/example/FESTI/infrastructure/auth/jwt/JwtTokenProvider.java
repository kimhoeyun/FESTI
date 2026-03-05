package com.example.FESTI.infrastructure.auth.jwt;

import com.example.FESTI.application.auth.AuthException;
import com.example.FESTI.application.auth.dto.TokenPair;
import com.example.FESTI.config.AuthProperties;
import com.example.FESTI.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final AuthProperties authProperties;
    private final Clock clock;
    private final SecretKey secretKey;

    public JwtTokenProvider(AuthProperties authProperties, Clock clock) {
        this.authProperties = authProperties;
        this.clock = clock;

        String secret = authProperties.getJwt().getSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair issueTokenPair(User user) {
        Instant now = Instant.now(clock);
        Instant accessExp = now.plus(authProperties.getJwt().getAccessTokenMinutes(), ChronoUnit.MINUTES);
        Instant refreshExp = now.plus(authProperties.getJwt().getRefreshTokenDays(), ChronoUnit.DAYS);

        String access = buildToken(user.getId(), user.getRole().name(), "access", now, accessExp);
        String refresh = buildToken(user.getId(), user.getRole().name(), "refresh", now, refreshExp);

        return new TokenPair(access, refresh, accessExp, refreshExp);
    }

    public Long parseUserIdFromAccessToken(String token) {
        Claims claims = parseClaims(token, "access");
        return Long.parseLong(claims.getSubject());
    }

    public Long parseUserIdFromRefreshToken(String token) {
        Claims claims = parseClaims(token, "refresh");
        return Long.parseLong(claims.getSubject());
    }

    private Claims parseClaims(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .clock(() -> Date.from(Instant.now(clock)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String actualType = claims.get("typ", String.class);
            if (!expectedType.equals(actualType)) {
                throw new AuthException("token type mismatch");
            }
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException("invalid token", e);
        }
    }

    private String buildToken(Long userId, String role, String type, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("typ", type)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
}

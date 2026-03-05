package com.example.FESTI.infrastructure.security;

import com.example.FESTI.application.auth.dto.TokenPair;
import com.example.FESTI.config.AuthProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class AuthCookieManager {

    public static final String ACCESS_COOKIE = "festi_at";
    public static final String REFRESH_COOKIE = "festi_rt";
    public static final String OAUTH_STATE_COOKIE = "festi_oauth_state";

    private final AuthProperties authProperties;

    public AuthCookieManager(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public void writeAuthCookies(HttpServletResponse response, TokenPair tokenPair) {
        ResponseCookie accessCookie = baseCookie(ACCESS_COOKIE, tokenPair.accessToken())
                .path("/")
                .maxAge(Duration.between(Instant.now(), tokenPair.accessExpiresAt()))
                .build();

        ResponseCookie refreshCookie = baseCookie(REFRESH_COOKIE, tokenPair.refreshToken())
                .path("/api/v1/auth")
                .maxAge(Duration.between(Instant.now(), tokenPair.refreshExpiresAt()))
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", baseCookie(ACCESS_COOKIE, "").path("/").maxAge(Duration.ZERO).build().toString());
        response.addHeader("Set-Cookie", baseCookie(REFRESH_COOKIE, "").path("/api/v1/auth").maxAge(Duration.ZERO).build().toString());
    }

    public void writeStateCookie(HttpServletResponse response, String state) {
        ResponseCookie cookie = baseCookie(OAUTH_STATE_COOKIE, state)
                .path("/api/v1/auth/oauth2")
                .maxAge(Duration.ofMinutes(5))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearStateCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", baseCookie(OAUTH_STATE_COOKIE, "")
                .path("/api/v1/auth/oauth2")
                .maxAge(Duration.ZERO)
                .build()
                .toString());
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(authProperties.getCookie().isSecure())
                .sameSite(authProperties.getCookie().getSameSite());
    }
}

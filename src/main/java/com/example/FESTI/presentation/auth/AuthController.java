package com.example.FESTI.presentation.auth;

import com.example.FESTI.application.auth.AuthException;
import com.example.FESTI.application.auth.LogoutUseCase;
import com.example.FESTI.application.auth.OAuthLoginUseCase;
import com.example.FESTI.application.auth.RefreshTokenUseCase;
import com.example.FESTI.application.auth.dto.LoginResult;
import com.example.FESTI.application.auth.dto.TokenPair;
import com.example.FESTI.config.AuthProperties;
import com.example.FESTI.domain.auth.entity.OAuthProvider;
import com.example.FESTI.infrastructure.security.AuthCookieManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final OAuthLoginUseCase oAuthLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final AuthCookieManager authCookieManager;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthController(OAuthLoginUseCase oAuthLoginUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          AuthCookieManager authCookieManager,
                          AuthProperties authProperties) {
        this.oAuthLoginUseCase = oAuthLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.authCookieManager = authCookieManager;
        this.authProperties = authProperties;
    }

    @GetMapping("/oauth2/{provider}/start")
    public ResponseEntity<Void> start(@PathVariable String provider,
                                      jakarta.servlet.http.HttpServletResponse response) {
        OAuthProvider oauthProvider = OAuthProvider.from(provider);
        String state = generateState();
        String authorizeUrl = oAuthLoginUseCase.buildAuthorizeUrl(oauthProvider, state);
        authCookieManager.writeStateCookie(response, state);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authorizeUrl))
                .build();
    }

    @GetMapping("/oauth2/{provider}/callback")
    public ResponseEntity<Void> callback(@PathVariable String provider,
                                         @RequestParam("code") String code,
                                         @RequestParam("state") String state,
                                         @CookieValue(name = AuthCookieManager.OAUTH_STATE_COOKIE, required = false) String savedState,
                                         jakarta.servlet.http.HttpServletResponse response) {
        if (savedState == null || !savedState.equals(state)) {
            authCookieManager.clearStateCookie(response);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(authProperties.getRedirect().getFailUrl()))
                    .build();
        }

        try {
            OAuthProvider oauthProvider = OAuthProvider.from(provider);
            LoginResult loginResult = oAuthLoginUseCase.login(oauthProvider, code);
            authCookieManager.writeAuthCookies(response, loginResult.tokenPair());
            authCookieManager.clearStateCookie(response);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(authProperties.getRedirect().getSuccessUrl()))
                    .build();
        } catch (AuthException | IllegalArgumentException e) {
            authCookieManager.clearStateCookie(response);
            authCookieManager.clearAuthCookies(response);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(authProperties.getRedirect().getFailUrl()))
                    .build();
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name = AuthCookieManager.REFRESH_COOKIE) String refreshToken,
                                        jakarta.servlet.http.HttpServletResponse response) {
        TokenPair tokenPair = refreshTokenUseCase.rotate(refreshToken);
        authCookieManager.writeAuthCookies(response, tokenPair);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = AuthCookieManager.REFRESH_COOKIE, required = false) String refreshToken,
                                       jakarta.servlet.http.HttpServletResponse response) {
        logoutUseCase.logout(refreshToken);
        authCookieManager.clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    private String generateState() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

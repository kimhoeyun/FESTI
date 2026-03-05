package com.example.FESTI.infrastructure.auth.oauth;

import com.example.FESTI.application.auth.AuthException;
import com.example.FESTI.application.auth.dto.OAuthUserInfo;
import com.example.FESTI.config.AuthProperties;
import com.example.FESTI.domain.auth.entity.OAuthProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

@Component
@Profile("!test")
public class GoogleOAuthProviderClient implements OAuthProviderClient {

    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestClient restClient;
    private final AuthProperties authProperties;

    public GoogleOAuthProviderClient(RestClient restClient, AuthProperties authProperties) {
        this.restClient = restClient;
        this.authProperties = authProperties;
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", authProperties.getOauth().getGoogle().getClientId())
                .queryParam("redirect_uri", callbackUri())
                .queryParam("scope", "openid profile email")
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String code) {
        GoogleTokenResponse tokenResponse = exchangeCode(code);
        GoogleUserInfoResponse userInfo = restClient.get()
                .uri(USERINFO_URL)
                .headers(headers -> headers.setBearerAuth(tokenResponse.accessToken()))
                .retrieve()
                .body(GoogleUserInfoResponse.class);

        if (userInfo == null || userInfo.sub() == null || userInfo.sub().isBlank()) {
            throw new AuthException("google user info is invalid");
        }
        return new OAuthUserInfo(userInfo.sub(), userInfo.email(), userInfo.name());
    }

    private GoogleTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", authProperties.getOauth().getGoogle().getClientId());
        form.add("client_secret", authProperties.getOauth().getGoogle().getClientSecret());
        form.add("redirect_uri", callbackUri());
        form.add("code", code);

        try {
            GoogleTokenResponse response = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenResponse.class);
            return Objects.requireNonNull(response, "google token response is null");
        } catch (Exception e) {
            throw new AuthException("google token exchange failed", e);
        }
    }

    private String callbackUri() {
        return authProperties.getOauth().getCallbackBaseUrl() + "/api/v1/auth/oauth2/google/callback";
    }

    private record GoogleTokenResponse(String access_token) {
        String accessToken() {
            return access_token;
        }
    }

    private record GoogleUserInfoResponse(String sub, String email, String name) {
    }
}

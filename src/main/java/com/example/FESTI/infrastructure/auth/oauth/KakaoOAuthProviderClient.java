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
public class KakaoOAuthProviderClient implements OAuthProviderClient {

    private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient;
    private final AuthProperties authProperties;

    public KakaoOAuthProviderClient(RestClient restClient, AuthProperties authProperties) {
        this.restClient = restClient;
        this.authProperties = authProperties;
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", authProperties.getOauth().getKakao().getClientId())
                .queryParam("redirect_uri", callbackUri())
                .queryParam("scope", "profile_nickname account_email")
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String code) {
        KakaoTokenResponse tokenResponse = exchangeCode(code);
        KakaoUserInfoResponse userInfo = restClient.get()
                .uri(USERINFO_URL)
                .headers(headers -> headers.setBearerAuth(tokenResponse.accessToken()))
                .retrieve()
                .body(KakaoUserInfoResponse.class);

        if (userInfo == null || userInfo.id() == null) {
            throw new AuthException("kakao user info is invalid");
        }

        String email = userInfo.kakaoAccount() == null ? null : userInfo.kakaoAccount().email();
        String name = userInfo.properties() == null ? null : userInfo.properties().nickname();
        return new OAuthUserInfo(String.valueOf(userInfo.id()), email, name);
    }

    private KakaoTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", authProperties.getOauth().getKakao().getClientId());
        form.add("client_secret", authProperties.getOauth().getKakao().getClientSecret());
        form.add("redirect_uri", callbackUri());
        form.add("code", code);

        try {
            KakaoTokenResponse response = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
            return Objects.requireNonNull(response, "kakao token response is null");
        } catch (Exception e) {
            throw new AuthException("kakao token exchange failed", e);
        }
    }

    private String callbackUri() {
        return authProperties.getOauth().getCallbackBaseUrl() + "/api/v1/auth/oauth2/kakao/callback";
    }

    private record KakaoTokenResponse(String access_token) {
        String accessToken() {
            return access_token;
        }
    }

    private record KakaoUserInfoResponse(Long id, KakaoAccount kakao_account, Properties properties) {
        KakaoAccount kakaoAccount() {
            return kakao_account;
        }

        record KakaoAccount(String email) {
        }

        record Properties(String nickname) {
        }
    }
}

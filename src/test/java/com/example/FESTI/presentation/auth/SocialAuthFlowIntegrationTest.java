package com.example.FESTI.presentation.auth;

import com.example.FESTI.application.auth.AuthException;
import com.example.FESTI.application.auth.dto.OAuthUserInfo;
import com.example.FESTI.domain.auth.entity.OAuthProvider;
import com.example.FESTI.domain.auth.repository.RefreshTokenRepository;
import com.example.FESTI.domain.auth.repository.SocialAccountRepository;
import com.example.FESTI.domain.user.repository.UserRepository;
import com.example.FESTI.infrastructure.auth.oauth.OAuthProviderClient;
import com.example.FESTI.infrastructure.security.AuthCookieManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SocialAuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        socialAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void googleLoginFlowWorksWithCookies() throws Exception {
        String state = startAndExtractState("google");
        MvcResult callbackResult = callback("google", "google-user-1", state);

        assertThat(callbackResult.getResponse().getStatus()).isEqualTo(302);
        assertThat(callbackResult.getResponse().getHeader(HttpHeaders.LOCATION)).isEqualTo("/auth/success");

        String accessToken = extractCookieValue(callbackResult, AuthCookieManager.ACCESS_COOKIE);
        String refreshToken = extractCookieValue(callbackResult, AuthCookieManager.REFRESH_COOKIE);
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        mockMvc.perform(get("/api/v1/users/me")
                        .cookie(new Cookie(AuthCookieManager.ACCESS_COOKIE, accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.profileCompleted").value(false));

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .cookie(new Cookie(AuthCookieManager.REFRESH_COOKIE, refreshToken)))
                .andExpect(status().isNoContent())
                .andReturn();

        String rotatedRefresh = extractCookieValue(refreshResult, AuthCookieManager.REFRESH_COOKIE);
        assertThat(rotatedRefresh).isNotBlank();
        assertThat(rotatedRefresh).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie(AuthCookieManager.REFRESH_COOKIE, rotatedRefresh)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .cookie(new Cookie(AuthCookieManager.REFRESH_COOKIE, rotatedRefresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void callbackWithInvalidStateRedirectsFail() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth2/google/callback")
                        .param("code", "google-user-1")
                        .param("state", "invalid")
                        .cookie(new Cookie(AuthCookieManager.OAUTH_STATE_COOKIE, "another-state")))
                .andExpect(status().isFound())
                .andExpect(result -> assertThat(result.getResponse().getHeader(HttpHeaders.LOCATION)).isEqualTo("/auth/fail"));
    }

    @Test
    void callbackWithIllegalArgumentRedirectsFail() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth2/unknown/callback")
                        .param("code", "google-user-1")
                        .param("state", "state-ok")
                        .cookie(new Cookie(AuthCookieManager.OAUTH_STATE_COOKIE, "state-ok")))
                .andExpect(status().isFound())
                .andExpect(result -> assertThat(result.getResponse().getHeader(HttpHeaders.LOCATION)).isEqualTo("/auth/fail"));
    }

    @Test
    void callbackWithAuthExceptionRedirectsFail() throws Exception {
        String state = startAndExtractState("google");
        mockMvc.perform(get("/api/v1/auth/oauth2/google/callback")
                        .param("code", "auth-error")
                        .param("state", state)
                        .cookie(new Cookie(AuthCookieManager.OAUTH_STATE_COOKIE, state)))
                .andExpect(status().isFound())
                .andExpect(result -> assertThat(result.getResponse().getHeader(HttpHeaders.LOCATION)).isEqualTo("/auth/fail"));
    }

    @Test
    void callbackWithRuntimeExceptionReturns500Json() throws Exception {
        String state = startAndExtractState("google");
        mockMvc.perform(get("/api/v1/auth/oauth2/google/callback")
                        .param("code", "runtime-error")
                        .param("state", state)
                        .cookie(new Cookie(AuthCookieManager.OAUTH_STATE_COOKIE, state)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    void duplicateCallbackUsesSingleSocialAccount() throws Exception {
        String state1 = startAndExtractState("google");
        callback("google", "duplicate-user", state1);

        String state2 = startAndExtractState("google");
        callback("google", "duplicate-user", state2);

        assertThat(socialAccountRepository.count()).isEqualTo(1);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void duplicateCellphoneReturnsConflict() throws Exception {
        String firstState = startAndExtractState("google");
        MvcResult firstCallback = callback("google", "phone-user-1", firstState);
        String firstAccess = extractCookieValue(firstCallback, AuthCookieManager.ACCESS_COOKIE);

        mockMvc.perform(post("/api/v1/users/me/cellphone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(AuthCookieManager.ACCESS_COOKIE, firstAccess))
                        .content("{\"cellphone\":\"01012345678\"}"))
                .andExpect(status().isNoContent());

        String secondState = startAndExtractState("kakao");
        MvcResult secondCallback = callback("kakao", "phone-user-2", secondState);
        String secondAccess = extractCookieValue(secondCallback, AuthCookieManager.ACCESS_COOKIE);

        mockMvc.perform(post("/api/v1/users/me/cellphone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(AuthCookieManager.ACCESS_COOKIE, secondAccess))
                        .content("{\"cellphone\":\"01012345678\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void meWithoutAccessTokenReturnsUnauthorizedJson() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    private String startAndExtractState(String provider) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/oauth2/{provider}/start", provider))
                .andExpect(status().isFound())
                .andReturn();
        return extractCookieValue(result, AuthCookieManager.OAUTH_STATE_COOKIE);
    }

    private MvcResult callback(String provider, String code, String state) throws Exception {
        return mockMvc.perform(get("/api/v1/auth/oauth2/{provider}/callback", provider)
                        .param("code", code)
                        .param("state", state)
                        .cookie(new Cookie(AuthCookieManager.OAUTH_STATE_COOKIE, state)))
                .andExpect(status().isFound())
                .andReturn();
    }

    private String extractCookieValue(MvcResult result, String cookieName) {
        List<String> setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        for (String header : setCookies) {
            if (header.startsWith(cookieName + "=")) {
                String withoutName = header.substring(cookieName.length() + 1);
                int delimiter = withoutName.indexOf(';');
                return delimiter >= 0 ? withoutName.substring(0, delimiter) : withoutName;
            }
        }
        return null;
    }

    @TestConfiguration
    static class OAuthClientStubConfig {

        @Bean
        @Primary
        OAuthProviderClient googleOAuthStubClient() {
            return new OAuthProviderClient() {
                @Override
                public OAuthProvider provider() {
                    return OAuthProvider.GOOGLE;
                }

                @Override
                public String buildAuthorizeUrl(String state) {
                    return "https://google.example/auth?state=" + state;
                }

                @Override
                public OAuthUserInfo fetchUserInfo(String code) {
                    if ("auth-error".equals(code)) {
                        throw new AuthException("oauth auth failure");
                    }
                    if ("runtime-error".equals(code)) {
                        throw new RuntimeException("unexpected oauth runtime error");
                    }
                    return new OAuthUserInfo(code, code + "@gmail.com", "google-" + code);
                }
            };
        }

        @Bean
        @Primary
        OAuthProviderClient kakaoOAuthStubClient() {
            return new OAuthProviderClient() {
                @Override
                public OAuthProvider provider() {
                    return OAuthProvider.KAKAO;
                }

                @Override
                public String buildAuthorizeUrl(String state) {
                    return "https://kakao.example/auth?state=" + state;
                }

                @Override
                public OAuthUserInfo fetchUserInfo(String code) {
                    if ("auth-error".equals(code)) {
                        throw new AuthException("oauth auth failure");
                    }
                    if ("runtime-error".equals(code)) {
                        throw new RuntimeException("unexpected oauth runtime error");
                    }
                    return new OAuthUserInfo(code, code + "@kakao.com", "kakao-" + code);
                }
            };
        }
    }
}

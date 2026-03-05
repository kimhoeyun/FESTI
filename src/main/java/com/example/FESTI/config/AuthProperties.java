package com.example.FESTI.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private final Jwt jwt = new Jwt();
    private final Cookie cookie = new Cookie();
    private final Redirect redirect = new Redirect();
    private final Oauth oauth = new Oauth();

    public Jwt getJwt() {
        return jwt;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public Redirect getRedirect() {
        return redirect;
    }

    public Oauth getOauth() {
        return oauth;
    }

    public static class Jwt {
        private String secret;
        private long accessTokenMinutes = 15;
        private long refreshTokenDays = 14;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessTokenMinutes() {
            return accessTokenMinutes;
        }

        public void setAccessTokenMinutes(long accessTokenMinutes) {
            this.accessTokenMinutes = accessTokenMinutes;
        }

        public long getRefreshTokenDays() {
            return refreshTokenDays;
        }

        public void setRefreshTokenDays(long refreshTokenDays) {
            this.refreshTokenDays = refreshTokenDays;
        }
    }

    public static class Cookie {
        private boolean secure;
        private String sameSite = "Lax";

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        public String getSameSite() {
            return sameSite;
        }

        public void setSameSite(String sameSite) {
            this.sameSite = sameSite;
        }
    }

    public static class Redirect {
        private String successUrl = "/auth/success";
        private String failUrl = "/auth/fail";

        public String getSuccessUrl() {
            return successUrl;
        }

        public void setSuccessUrl(String successUrl) {
            this.successUrl = successUrl;
        }

        public String getFailUrl() {
            return failUrl;
        }

        public void setFailUrl(String failUrl) {
            this.failUrl = failUrl;
        }
    }

    public static class Oauth {
        private String callbackBaseUrl;
        private final Client google = new Client();
        private final Client kakao = new Client();

        public String getCallbackBaseUrl() {
            return callbackBaseUrl;
        }

        public void setCallbackBaseUrl(String callbackBaseUrl) {
            this.callbackBaseUrl = callbackBaseUrl;
        }

        public Client getGoogle() {
            return google;
        }

        public Client getKakao() {
            return kakao;
        }
    }

    public static class Client {
        private String clientId;
        private String clientSecret;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }
}

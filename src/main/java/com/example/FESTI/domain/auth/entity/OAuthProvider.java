package com.example.FESTI.domain.auth.entity;

public enum OAuthProvider {
    GOOGLE,
    KAKAO;

    public static OAuthProvider from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("provider must not be null");
        }
        return switch (value.toLowerCase()) {
            case "google" -> GOOGLE;
            case "kakao" -> KAKAO;
            default -> throw new IllegalArgumentException("unsupported provider: " + value);
        };
    }
}

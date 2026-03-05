package com.example.FESTI.application.auth.dto;

public record OAuthUserInfo(
        String providerUserId,
        String email,
        String name
) {
}

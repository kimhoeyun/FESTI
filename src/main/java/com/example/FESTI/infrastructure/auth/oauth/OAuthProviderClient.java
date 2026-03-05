package com.example.FESTI.infrastructure.auth.oauth;

import com.example.FESTI.application.auth.dto.OAuthUserInfo;
import com.example.FESTI.domain.auth.entity.OAuthProvider;

public interface OAuthProviderClient {

    OAuthProvider provider();

    String buildAuthorizeUrl(String state);

    OAuthUserInfo fetchUserInfo(String code);
}

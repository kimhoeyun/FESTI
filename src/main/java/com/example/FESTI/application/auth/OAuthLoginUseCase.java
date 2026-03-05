package com.example.FESTI.application.auth;

import com.example.FESTI.application.auth.dto.LoginResult;
import com.example.FESTI.application.auth.dto.OAuthUserInfo;
import com.example.FESTI.application.auth.dto.TokenPair;
import com.example.FESTI.domain.auth.entity.OAuthProvider;
import com.example.FESTI.domain.auth.entity.RefreshToken;
import com.example.FESTI.domain.auth.entity.SocialAccount;
import com.example.FESTI.domain.auth.repository.RefreshTokenRepository;
import com.example.FESTI.domain.auth.repository.SocialAccountRepository;
import com.example.FESTI.domain.user.entity.Role;
import com.example.FESTI.domain.user.entity.User;
import com.example.FESTI.domain.user.repository.UserRepository;
import com.example.FESTI.infrastructure.auth.jwt.JwtTokenProvider;
import com.example.FESTI.infrastructure.auth.jwt.TokenHashService;
import com.example.FESTI.infrastructure.auth.oauth.OAuthProviderClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class OAuthLoginUseCase {

    private final Map<OAuthProvider, OAuthProviderClient> clients;
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashService tokenHashService;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public OAuthLoginUseCase(List<OAuthProviderClient> clients,
                             SocialAccountRepository socialAccountRepository,
                             UserRepository userRepository,
                             RefreshTokenRepository refreshTokenRepository,
                             JwtTokenProvider jwtTokenProvider,
                             TokenHashService tokenHashService,
                             Clock clock,
                             TransactionTemplate transactionTemplate) {
        this.clients = new EnumMap<>(OAuthProvider.class);
        clients.forEach(client -> this.clients.put(client.provider(), client));
        this.socialAccountRepository = socialAccountRepository;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenHashService = tokenHashService;
        this.clock = clock;
        this.transactionTemplate = transactionTemplate;
    }

    public String buildAuthorizeUrl(OAuthProvider provider, String state) {
        return client(provider).buildAuthorizeUrl(state);
    }

    public LoginResult login(OAuthProvider provider, String code) {
        OAuthUserInfo userInfo = client(provider).fetchUserInfo(code);
        LoginResult result = transactionTemplate.execute(status -> issueTokenForUser(provider, userInfo));
        if (result == null) {
            throw new AuthException("login transaction failed");
        }
        return result;
    }

    private LoginResult issueTokenForUser(OAuthProvider provider, OAuthUserInfo userInfo) {
        User user = socialAccountRepository.findByProviderAndProviderUserId(provider, userInfo.providerUserId())
                .map(SocialAccount::getUser)
                .orElseGet(() -> createUserAndSocialAccount(provider, userInfo));

        TokenPair tokenPair = jwtTokenProvider.issueTokenPair(user);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.hash(tokenPair.refreshToken()),
                LocalDateTime.ofInstant(tokenPair.refreshExpiresAt(), ZoneOffset.UTC),
                null
        );
        refreshTokenRepository.save(refreshToken);
        return new LoginResult(user, tokenPair);
    }

    private User createUserAndSocialAccount(OAuthProvider provider, OAuthUserInfo userInfo) {
        User user = new User(Role.CUSTOMER, fallbackName(userInfo), null);
        User savedUser = userRepository.save(user);

        try {
            SocialAccount socialAccount = new SocialAccount(savedUser, provider, userInfo.providerUserId(), userInfo.email());
            socialAccountRepository.save(socialAccount);
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            return socialAccountRepository.findByProviderAndProviderUserId(provider, userInfo.providerUserId())
                    .map(SocialAccount::getUser)
                    .orElseThrow(() -> new AuthException("social account upsert failed", e));
        }
    }

    private String fallbackName(OAuthUserInfo userInfo) {
        if (userInfo.name() != null && !userInfo.name().isBlank()) {
            return userInfo.name();
        }
        return "user-" + userInfo.providerUserId();
    }

    private OAuthProviderClient client(OAuthProvider provider) {
        OAuthProviderClient client = clients.get(provider);
        if (client == null) {
            throw new AuthException("oauth provider is not configured: " + provider);
        }
        return client;
    }
}

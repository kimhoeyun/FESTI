package com.example.FESTI.application.auth;

import com.example.FESTI.domain.auth.repository.RefreshTokenRepository;
import com.example.FESTI.infrastructure.auth.jwt.TokenHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashService tokenHashService;
    private final Clock clock;

    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository,
                         TokenHashService tokenHashService,
                         Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHashService = tokenHashService;
        this.clock = clock;
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(tokenHashService.hash(rawRefreshToken))
                .ifPresent(refreshToken -> refreshToken.revoke(LocalDateTime.now(clock)));
    }
}

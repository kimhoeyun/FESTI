package com.example.FESTI.application.auth;

import com.example.FESTI.application.auth.dto.TokenPair;
import com.example.FESTI.domain.auth.entity.RefreshToken;
import com.example.FESTI.domain.auth.repository.RefreshTokenRepository;
import com.example.FESTI.domain.user.entity.User;
import com.example.FESTI.infrastructure.auth.jwt.JwtTokenProvider;
import com.example.FESTI.infrastructure.auth.jwt.TokenHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashService tokenHashService;
    private final Clock clock;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                               JwtTokenProvider jwtTokenProvider,
                               TokenHashService tokenHashService,
                               Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenHashService = tokenHashService;
        this.clock = clock;
    }

    @Transactional
    public TokenPair rotate(String rawRefreshToken) {
        Long userIdFromToken = jwtTokenProvider.parseUserIdFromRefreshToken(rawRefreshToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHashService.hash(rawRefreshToken))
                .orElseThrow(() -> new AuthException("refresh token not found"));

        LocalDateTime now = LocalDateTime.now(clock);
        if (existing.isRevoked() || existing.isExpired(now)) {
            throw new AuthException("refresh token is not active");
        }
        if (!existing.getUser().getId().equals(userIdFromToken)) {
            throw new AuthException("refresh token user mismatch");
        }

        existing.revoke(now);
        User user = existing.getUser();

        TokenPair newPair = jwtTokenProvider.issueTokenPair(user);
        RefreshToken rotated = new RefreshToken(
                user,
                tokenHashService.hash(newPair.refreshToken()),
                LocalDateTime.ofInstant(newPair.refreshExpiresAt(), ZoneOffset.UTC),
                existing
        );
        refreshTokenRepository.save(rotated);
        return newPair;
    }
}

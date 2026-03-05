package com.example.FESTI.domain.auth.repository;

import com.example.FESTI.domain.auth.entity.RefreshToken;
import com.example.FESTI.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    int deleteByUserAndRevokedAtIsNotNullAndUpdatedAtBefore(User user, LocalDateTime threshold);
}

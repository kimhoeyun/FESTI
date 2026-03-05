package com.example.FESTI.infrastructure.auth.jwt;

import com.example.FESTI.application.auth.AuthException;
import com.example.FESTI.config.AuthProperties;
import com.example.FESTI.domain.user.entity.Role;
import com.example.FESTI.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    @Test
    void issueAndParseTokens() throws Exception {
        JwtTokenProvider provider = new JwtTokenProvider(authProperties(), fixedClock());
        User user = userWithId(1L);

        var tokenPair = provider.issueTokenPair(user);

        assertEquals(1L, provider.parseUserIdFromAccessToken(tokenPair.accessToken()));
        assertEquals(1L, provider.parseUserIdFromRefreshToken(tokenPair.refreshToken()));
    }

    @Test
    void tokenTypeMismatchThrows() throws Exception {
        JwtTokenProvider provider = new JwtTokenProvider(authProperties(), fixedClock());
        User user = userWithId(2L);

        var tokenPair = provider.issueTokenPair(user);

        assertThrows(AuthException.class, () -> provider.parseUserIdFromRefreshToken(tokenPair.accessToken()));
    }

    @Test
    void invalidSignatureThrows() throws Exception {
        JwtTokenProvider provider = new JwtTokenProvider(authProperties(), fixedClock());
        User user = userWithId(3L);

        var tokenPair = provider.issueTokenPair(user);
        String brokenToken = tokenPair.accessToken() + "broken";

        assertThrows(AuthException.class, () -> provider.parseUserIdFromAccessToken(brokenToken));
    }

    private AuthProperties authProperties() {
        AuthProperties properties = new AuthProperties();
        properties.getJwt().setSecret("01234567890123456789012345678901");
        properties.getJwt().setAccessTokenMinutes(15);
        properties.getJwt().setRefreshTokenDays(14);
        return properties;
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-03-05T00:00:00Z"), ZoneOffset.UTC);
    }

    private User userWithId(Long id) throws Exception {
        User user = new User(Role.CUSTOMER, "tester", null);
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        return user;
    }
}

package com.example.security;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBlacklistServiceTest {

    @Test
    void shouldBlacklistAndDetectToken() {
        TokenBlacklistService service = new TokenBlacklistService();
        String token = "valid-token";
        Date future = new Date(System.currentTimeMillis() + 60_000);

        service.blacklistToken(token, future);

        assertTrue(service.isBlacklisted(token));
    }

    @Test
    void shouldIgnoreNullInputs() {
        TokenBlacklistService service = new TokenBlacklistService();

        service.blacklistToken(null, new Date());
        service.blacklistToken("token", null);

        assertFalse(service.isBlacklisted(null));
        assertFalse(service.isBlacklisted("token"));
    }

    @Test
    void shouldCleanupExpiredTokensLazily() {
        TokenBlacklistService service = new TokenBlacklistService();
        String token = "expired-token";
        Date past = new Date(System.currentTimeMillis() - 1000);

        service.blacklistToken(token, past);

        assertFalse(service.isBlacklisted(token));
    }
}

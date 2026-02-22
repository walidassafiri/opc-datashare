package com.example.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthResponseTest {

    @Test
    void shouldSupportDefaultConstructorAndSetters() {
        AuthResponse response = new AuthResponse();
        response.setToken("abc");
        response.setTokenType("Bearer");

        assertEquals("abc", response.getToken());
        assertEquals("Bearer", response.getTokenType());
    }
}

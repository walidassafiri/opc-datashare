package com.example.security;

import com.example.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomUserDetailsTest {

    @Test
    void shouldExposeUserInformationAndFlags() {
        User user = new User("alice@example.com", "hashed-password");
        user.setId(7L);
        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("alice@example.com", details.getUsername());
        assertEquals("hashed-password", details.getPassword());
        assertEquals(7L, details.getId());
        assertTrue(details.getAuthorities().isEmpty());

        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());

        assertFalse(details.getAuthorities().iterator().hasNext());
    }
}

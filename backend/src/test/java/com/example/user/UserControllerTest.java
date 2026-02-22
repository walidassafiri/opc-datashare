package com.example.user;

import com.example.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserController controller = new UserController();

    @Test
    void meShouldReturnUnauthorizedWhenAuthenticationIsNull() {
        ResponseEntity<?> response = controller.me(null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void meShouldReturnUnauthorizedWhenPrincipalIsNotCustomUserDetails() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("not-a-user");

        ResponseEntity<?> response = controller.me(authentication);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void meShouldReturnUserDataWhenAuthenticated() {
        User user = new User("john@example.com", "hash");
        user.setId(42L);
        CustomUserDetails details = new CustomUserDetails(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(details);

        ResponseEntity<?> response = controller.me(authentication);
        assertEquals(200, response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey("email"));
        assertTrue(body.containsKey("id"));
        assertEquals("john@example.com", body.get("email"));
        assertEquals(42L, body.get("id"));
    }
}

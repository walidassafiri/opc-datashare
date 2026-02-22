package com.example.auth;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.exception.InvalidCredentialsException;
import com.example.service.UserService;
import com.example.user.User;
import com.example.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        User createdUser = new User("user@example.com", "hashed");
        when(userService.createUser(request)).thenReturn(createdUser);
        when(jwtUtil.generateToken("user@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        User user = new User("user@example.com", "hashed");
        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("user@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void loginShouldThrowWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("bad-password");

        User user = new User("user@example.com", "hashed");
        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(passwordEncoder.matches("bad-password", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}

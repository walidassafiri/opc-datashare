package com.example.service;

import com.example.dto.RegisterRequest;
import com.example.exception.EmailAlreadyExistsException;
import com.example.user.User;
import com.example.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("exists@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    void createUserShouldHashPasswordAndSaveUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(request);

        assertEquals("new@example.com", created.getEmail());
        assertEquals("hashed-password", created.getPasswordHash());
    }
}

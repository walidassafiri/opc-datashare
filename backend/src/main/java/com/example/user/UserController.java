package com.example.user;

import com.example.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        CustomUserDetails u = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok().body(java.util.Map.of("email", u.getUsername(), "id", u.getId()));
    }
}
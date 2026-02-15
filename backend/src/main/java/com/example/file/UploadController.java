package com.example.file;

import com.example.security.CustomUserDetails;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    private static final long MAX_SIZE = 1L * 1024 * 1024 * 1024; // 1 GB
    private static final List<String> FORBIDDEN_EXT = List.of("exe","bat","cmd","sh");

    @GetMapping("/download/{token}")
    public ResponseEntity<Resource> download(@PathVariable String token, @RequestParam(required = false) String password) {
        Resource resource = uploadService.loadAsResource(token, password);
        if (resource == null) {
            // On pourrait être plus précis : 404 si inexistant, 403 si mauvais mdp
            // Mais pour simplifier et éviter le leak d'existence, on peut rester sur 404 ou Unauthorized
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/info/{token}")
    public ResponseEntity<?> getInfo(@PathVariable String token) {
        FileMetadata meta = uploadService.getMetadata(token);
        if (meta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(meta);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long ownerId = userDetails.getId();
        
        if (ownerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid owner ID");
        }

        return ResponseEntity.ok(uploadService.getHistory(ownerId));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(required = false) Integer expirationDays,
                                    @RequestParam(required = false) String password,
                                    @RequestParam(required = false) List<String> tags,
                                    Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }

        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest().body("File too large");
        }

        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            String ext = name.substring(dot+1).toLowerCase();
            if (FORBIDDEN_EXT.contains(ext)) {
                return ResponseEntity.badRequest().body("Forbidden file type");
            }
        }

        if (password != null && password.length() > 0 && password.length() < 6) {
            return ResponseEntity.badRequest().body("Password too short");
        }

        if (expirationDays != null && expirationDays > 7) {
            return ResponseEntity.badRequest().body("Expiration too long");
        }

        UploadRequest req = new UploadRequest();
        req.setExpirationDays(expirationDays);
        req.setPassword(password);
        req.setTags(tags);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long ownerId = userDetails.getId();

        FileMetadata meta = uploadService.store(file, ownerId, req);
        if (meta == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(meta);
    }
}

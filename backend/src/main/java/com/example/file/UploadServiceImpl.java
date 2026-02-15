package com.example.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    private final Path storageDir;
    private final FileMetadataRepository repository;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UploadServiceImpl(FileMetadataRepository repository, Clock clock, PasswordEncoder passwordEncoder) throws IOException {
        this.repository = repository;
        this.clock = clock;
        this.passwordEncoder = passwordEncoder;
        this.storageDir = Path.of("uploads");
        if (!Files.exists(storageDir)) Files.createDirectories(storageDir);
    }

    @Override
    public FileMetadata store(MultipartFile file, Long ownerId, UploadRequest request) {
        try {
            String token = UUID.randomUUID().toString();
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String filename = token + "_" + original.replaceAll("[^a-zA-Z0-9._-]","_");
            Path target = storageDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            int days = 7;
            if (request != null && request.getExpirationDays() != null) {
                days = Math.min(7, Math.max(0, request.getExpirationDays()));
            }
            LocalDateTime expires = LocalDateTime.now(clock).plusDays(days);

            FileMetadata meta = new FileMetadata(token, original, file.getSize(), expires, request == null ? null : request.getTags(), ownerId);
            if (request != null && request.getPassword() != null && !request.getPassword().isEmpty()) {
                meta.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            return repository.save(meta);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public List<FileMetadata> getHistory(Long ownerId) {
        cleanupExpiredFiles();
        if (ownerId == null) return List.of();
        return repository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Override
    public Resource loadAsResource(String token) {
        return loadAsResource(token, null);
    }

    @Override
    public Resource loadAsResource(String token, String password) {
        cleanupExpiredFiles();
        FileMetadata meta = repository.findById(token).orElse(null);
        if (meta == null || meta.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
            return null;
        }

        if (meta.getPassword() != null && !meta.getPassword().isEmpty()) {
            if (password == null || !passwordEncoder.matches(password, meta.getPassword())) {
                return null;
            }
        }

        try {
            String filename = token + "_" + meta.getFilename().replaceAll("[^a-zA-Z0-9._-]","_");
            Path file = storageDir.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public FileMetadata getMetadata(String token) {
        cleanupExpiredFiles();
        FileMetadata meta = repository.findById(token).orElse(null);
        if (meta == null || meta.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
            return null;
        }
        // Masquer le mot de passe dans les métadonnées retournées
        FileMetadata dto = new FileMetadata(meta.getToken(), meta.getFilename(), meta.getSize(), meta.getExpiresAt(), meta.getTags(), meta.getOwnerId());
        dto.setCreatedAt(meta.getCreatedAt());
        // On met une valeur factice si un mot de passe est requis
        if (meta.getPassword() != null && !meta.getPassword().isEmpty()) {
            dto.setPassword("PROTECTED");
        }
        return dto;
    }

    @Override
    public void cleanupExpiredFiles() {
        List<FileMetadata> expired = repository.findAllByExpiresAtBefore(LocalDateTime.now(clock));
        for (FileMetadata meta : expired) {
            try {
                String filename = meta.getToken() + "_" + meta.getFilename().replaceAll("[^a-zA-Z0-9._-]","_");
                Path file = storageDir.resolve(filename);
                Files.deleteIfExists(file);
                repository.delete(meta);
            } catch (IOException e) {
                // log error or continue
            }
        }
    }
}

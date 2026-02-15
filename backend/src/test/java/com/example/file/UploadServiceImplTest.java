package com.example.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UploadServiceImplTest {

    private FileMetadataRepository repository;
    private UploadServiceImpl uploadService;
    private Path tempStorage;
    private Clock clock;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        repository = mock(FileMetadataRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        tempStorage = tempDir.resolve("uploads");
        Files.createDirectories(tempStorage);
        
        // On commence à un instant T
        clock = Clock.fixed(Instant.parse("2026-02-15T10:00:00Z"), ZoneId.of("UTC"));
        
        // On crée une sous-classe ou on injecte le path pour le test
        uploadService = new UploadServiceImpl(repository, clock, passwordEncoder) {
            // On override le directory de stockage pour utiliser le dossier temporaire de JUnit
            {
                try {
                    var field = UploadServiceImpl.class.getDeclaredField("storageDir");
                    field.setAccessible(true);
                    field.set(this, tempStorage);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    void shouldCleanupExpiredFilesOnLoadAsResource() throws IOException {
        // GIVEN
        String token = "expired-token";
        LocalDateTime expiredDate = LocalDateTime.now(clock).minusMinutes(1);
        FileMetadata meta = new FileMetadata(token, "test.txt", 10, expiredDate, List.of(), 1L);
        
        when(repository.findById(token)).thenReturn(Optional.of(meta));
        when(repository.findAllByExpiresAtBefore(any())).thenReturn(List.of(meta));

        // On simule l'existence du fichier physiquement
        Path filePath = tempStorage.resolve(token + "_test.txt");
        Files.write(filePath, "content".getBytes());
        assertTrue(Files.exists(filePath));

        // WHEN
        var resource = uploadService.loadAsResource(token);

        // THEN
        assertNull(resource, "La ressource devrait être nulle car expirée");
        verify(repository).findAllByExpiresAtBefore(any());
        verify(repository).delete(meta);
        assertFalse(Files.exists(filePath), "Le fichier physique devrait être supprimé");
    }

    @Test
    void shouldNotDeleteValidFilesOnLoadAsResource() throws IOException {
        // GIVEN
        String token = "valid-token";
        LocalDateTime futureDate = LocalDateTime.now(clock).plusDays(1);
        FileMetadata meta = new FileMetadata(token, "test.txt", 10, futureDate, List.of(), 1L);
        
        when(repository.findById(token)).thenReturn(Optional.of(meta));
        when(repository.findAllByExpiresAtBefore(any())).thenReturn(List.of());

        // On simule l'existence du fichier physiquement
        Path filePath = tempStorage.resolve(token + "_test.txt");
        Files.write(filePath, "content".getBytes());

        // WHEN
        var resource = uploadService.loadAsResource(token);

        // THEN
        assertNotNull(resource);
        assertTrue(Files.exists(filePath));
        verify(repository, never()).delete(any());
    }

    @Test
    void shouldStoreFileCorrectly() throws IOException {
        // GIVEN
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getSize()).thenReturn(10L);
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("content".getBytes()));
        
        UploadRequest req = new UploadRequest();
        req.setExpirationDays(3);
        req.setPassword("secret123");
        
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        FileMetadata meta = uploadService.store(file, 1L, req);

        // THEN
        assertNotNull(meta);
        assertEquals("test.txt", meta.getFilename());
        assertEquals(1L, meta.getOwnerId());
        // 2026-02-15T10:00:00Z + 3 jours = 2026-02-18T10:00:00Z
        assertEquals(LocalDateTime.now(clock).plusDays(3), meta.getExpiresAt());
        assertNotNull(meta.getPassword());
        assertTrue(passwordEncoder.matches("secret123", meta.getPassword()));
        
        Path expectedPath = tempStorage.resolve(meta.getToken() + "_test.txt");
        assertTrue(Files.exists(expectedPath));
    }

    @Test
    void shouldLoadResourceWithCorrectPassword() throws IOException {
        // GIVEN
        String token = "protected-token";
        String password = "mypassword";
        String encoded = passwordEncoder.encode(password);
        FileMetadata meta = new FileMetadata(token, "test.txt", 10, LocalDateTime.now(clock).plusDays(1), List.of(), 1L);
        meta.setPassword(encoded);
        
        when(repository.findById(token)).thenReturn(Optional.of(meta));
        
        Path filePath = tempStorage.resolve(token + "_test.txt");
        Files.write(filePath, "content".getBytes());

        // WHEN
        var resource = uploadService.loadAsResource(token, password);

        // THEN
        assertNotNull(resource);
    }

    @Test
    void shouldNotLoadResourceWithWrongPassword() throws IOException {
        // GIVEN
        String token = "protected-token";
        String password = "mypassword";
        FileMetadata meta = new FileMetadata(token, "test.txt", 10, LocalDateTime.now(clock).plusDays(1), List.of(), 1L);
        meta.setPassword(passwordEncoder.encode(password));
        
        when(repository.findById(token)).thenReturn(Optional.of(meta));

        // WHEN
        var resource = uploadService.loadAsResource(token, "wrong");

        // THEN
        assertNull(resource);
    }
}

package com.example.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    private final Path storageDir;
    private final FileMetadataRepository repository;

    public UploadServiceImpl(FileMetadataRepository repository) throws IOException {
        this.repository = repository;
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
            LocalDateTime expires = LocalDateTime.now().plusDays(days);

            FileMetadata meta = new FileMetadata(token, original, file.getSize(), expires, request == null ? null : request.getTags(), ownerId);
            return repository.save(meta);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public List<FileMetadata> getHistory(Long ownerId) {
        if (ownerId == null) return List.of();
        return repository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }
}

package com.example.file;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
    List<FileMetadata> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<FileMetadata> findAllByExpiresAtBefore(LocalDateTime now);
}

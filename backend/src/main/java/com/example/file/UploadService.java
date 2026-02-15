package com.example.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UploadService {
    FileMetadata store(MultipartFile file, Long ownerId, UploadRequest request);
    List<FileMetadata> getHistory(Long ownerId);
    Resource loadAsResource(String token);
    Resource loadAsResource(String token, String password);
    FileMetadata getMetadata(String token);
    void cleanupExpiredFiles();
}

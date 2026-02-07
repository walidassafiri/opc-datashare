package com.example.file;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    FileMetadata store(MultipartFile file, Long ownerId, UploadRequest request);
}

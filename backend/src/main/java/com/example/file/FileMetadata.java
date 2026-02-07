package com.example.file;

import java.time.LocalDateTime;
import java.util.List;

public class FileMetadata {
    private String token;
    private String filename;
    private long size;
    private LocalDateTime expiresAt;
    private List<String> tags;

    public FileMetadata() {}
    public FileMetadata(String token, String filename, long size, LocalDateTime expiresAt, List<String> tags) {
        this.token = token; this.filename = filename; this.size = size; this.expiresAt = expiresAt; this.tags = tags;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}

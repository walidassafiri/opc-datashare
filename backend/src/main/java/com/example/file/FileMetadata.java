package com.example.file;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {
    @Id
    private String token;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(nullable = false)
    private long size;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @ElementCollection
    @CollectionTable(name = "file_tags", joinColumns = @JoinColumn(name = "file_token"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public FileMetadata() {}
    
    public FileMetadata(String token, String filename, long size, LocalDateTime expiresAt, List<String> tags, Long ownerId) {
        this.token = token; 
        this.filename = filename; 
        this.size = size; 
        this.expiresAt = expiresAt; 
        this.tags = tags;
        this.ownerId = ownerId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.example.securefilesharing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_records")
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storagePath;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean deleted = false;

    public FileRecord() {}

    public String getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final FileRecord r = new FileRecord();
        public Builder originalFilename(String v) { r.originalFilename = v; return this; }
        public Builder storagePath(String v) { r.storagePath = v; return this; }
        public Builder contentType(String v) { r.contentType = v; return this; }
        public Builder fileSize(long v) { r.fileSize = v; return this; }
        public Builder uploadedAt(LocalDateTime v) { r.uploadedAt = v; return this; }
        public Builder expiresAt(LocalDateTime v) { r.expiresAt = v; return this; }
        public FileRecord build() { return r; }
    }
}

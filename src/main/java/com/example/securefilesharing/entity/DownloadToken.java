package com.example.securefilesharing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "download_tokens")
public class DownloadToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_record_id", nullable = false)
    private FileRecord fileRecord;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public DownloadToken() {}

    public String getToken() { return token; }
    public FileRecord getFileRecord() { return fileRecord; }
    public void setFileRecord(FileRecord fileRecord) { this.fileRecord = fileRecord; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final DownloadToken r = new DownloadToken();
        public Builder fileRecord(FileRecord v) { r.fileRecord = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
        public DownloadToken build() { return r; }
    }
}

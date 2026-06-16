package com.example.securefilesharing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_records")
public class OtpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String otp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_record_id", nullable = false)
    private FileRecord fileRecord;

    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public OtpRecord() {}

    public Long getId() { return id; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public FileRecord getFileRecord() { return fileRecord; }
    public void setFileRecord(FileRecord fileRecord) { this.fileRecord = fileRecord; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final OtpRecord r = new OtpRecord();
        public Builder otp(String v) { r.otp = v; return this; }
        public Builder fileRecord(FileRecord v) { r.fileRecord = v; return this; }
        public Builder used(boolean v) { r.used = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
        public Builder expiresAt(LocalDateTime v) { r.expiresAt = v; return this; }
        public OtpRecord build() { return r; }
    }
}

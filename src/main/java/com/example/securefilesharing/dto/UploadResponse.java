package com.example.securefilesharing.dto;

public class UploadResponse {
    private boolean success;
    private String message;
    private String otp;
    private String fileId;
    private String originalFilename;
    private long fileSizeBytes;
    private String uploadedAt;
    private String expiresAt;
    private String note;

    private UploadResponse() {}

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getOtp() { return otp; }
    public String getFileId() { return fileId; }
    public String getOriginalFilename() { return originalFilename; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public String getUploadedAt() { return uploadedAt; }
    public String getExpiresAt() { return expiresAt; }
    public String getNote() { return note; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final UploadResponse r = new UploadResponse();
        public Builder success(boolean v) { r.success = v; return this; }
        public Builder message(String v) { r.message = v; return this; }
        public Builder otp(String v) { r.otp = v; return this; }
        public Builder fileId(String v) { r.fileId = v; return this; }
        public Builder originalFilename(String v) { r.originalFilename = v; return this; }
        public Builder fileSizeBytes(long v) { r.fileSizeBytes = v; return this; }
        public Builder uploadedAt(String v) { r.uploadedAt = v; return this; }
        public Builder expiresAt(String v) { r.expiresAt = v; return this; }
        public Builder note(String v) { r.note = v; return this; }
        public UploadResponse build() { return r; }
    }
}
